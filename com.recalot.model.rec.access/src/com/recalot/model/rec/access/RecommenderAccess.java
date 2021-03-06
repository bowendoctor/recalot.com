// Copyright (C) 2016 Matthäus Schmedding
//
// This file is part of recalot.com.
//
// recalot.com is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// recalot.com is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with recalot.com. If not, see <http://www.gnu.org/licenses/>.

package com.recalot.model.rec.access;


import com.recalot.common.GenericServiceListener;
import com.recalot.common.Helper;
import com.recalot.common.builder.RecommenderBuilder;
import com.recalot.common.communication.DataSet;
import com.recalot.common.exceptions.AlreadyExistsException;
import com.recalot.common.exceptions.BaseException;
import com.recalot.common.exceptions.MissingArgumentException;
import com.recalot.common.exceptions.NotFoundException;
import com.recalot.common.communication.Message;
import com.recalot.common.interfaces.model.data.DataSource;
import com.recalot.common.interfaces.model.rec.Recommender;
import com.recalot.common.interfaces.model.rec.RecommenderInformation;
import org.osgi.framework.BundleContext;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author matthaeus.schmedding
 */
public class RecommenderAccess implements com.recalot.common.interfaces.model.rec.RecommenderAccess, Closeable {

    private final BundleContext context;
    private final GenericServiceListener<RecommenderBuilder> recommenderListener;
    private final ConcurrentHashMap<String, Recommender> recommender;
    private final ConcurrentHashMap<String, Thread> threads;


    public RecommenderAccess(BundleContext context) {
        this.context = context;
        this.recommenderListener = new GenericServiceListener<>(context, RecommenderBuilder.class.getName());
        this.recommender = new ConcurrentHashMap<>();
        this.threads = new ConcurrentHashMap<>();
        this.context.addServiceListener(recommenderListener);
    }

    @Override
    public void close() throws IOException {
        if(threads != null){
            threads.values().forEach(java.lang.Thread::interrupt);
        }

        if (recommenderListener != null) {
            this.context.removeServiceListener(recommenderListener);
        }
    }

    @Override
    public String getKey() {
        return "recommender-access";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public RecommenderInformation[] getRecommenders() throws BaseException {
        List<RecommenderInformation> allAvailableRecommender = new ArrayList<>();

        allAvailableRecommender.addAll(recommender.values());
        allAvailableRecommender.addAll(recommenderListener.getAll());

        return allAvailableRecommender.toArray(new RecommenderInformation[allAvailableRecommender.size()]);
    }

    @Override
    public Recommender getRecommender(String id) throws BaseException {
        return  recommender.get(id);
    }

    @Override
    public RecommenderBuilder getRecommenderBuilder(String id) throws BaseException {
        return recommenderListener.getInstance(id);
    }
    @Override
    public Recommender createRecommender(DataSource dataSource, Map<String, String> param) throws BaseException {
        return createRecommender(dataSource, "", param);
    }

    @Override
    public Recommender createRecommender(DataSource dataSource, String configPrefix, Map<String, String> param) throws BaseException {
        String id = param.get(Helper.Keys.RecommenderBuilderId);
        String wishedId = param.get(Helper.Keys.ID);

        if (wishedId == null || wishedId.equals("")) {
            throw new MissingArgumentException("The argument id is missing.");
        }

        if (recommender.containsKey(wishedId)) {
            throw new AlreadyExistsException("A recommender with the id %s already exists. Please define another id or use the updateRecommender function.", wishedId);
        }

        RecommenderBuilder builder = recommenderListener.getInstance(id);

        Recommender instance = builder.createInstance(wishedId, configPrefix, param);

        instance.setId(wishedId);

        Thread thread = new Thread() {
            public void run() {
                try {
                    instance.setState(RecommenderInformation.RecommenderState.TRAINING);
                    instance.setDataSourceId(dataSource.getSourceId());
                    instance.setDataSet(dataSource.getDataSet());
                    instance.train();
                    instance.setState(RecommenderInformation.RecommenderState.READY);

                    threads.remove(instance.getId());
                } catch (BaseException e) {
                    e.printStackTrace();
                }
            }
        };

        threads.put(instance.getId(), thread);
        thread.start();

        recommender.put(instance.getId(), instance);
        return instance;
    }

    @Override
    public Recommender updateRecommender(String id, DataSource dataSource, Map<String, String> param) throws BaseException {

        Recommender instance = recommender.get(id);

        if (instance == null) {
            throw new NotFoundException("Recommender with the id %s can not be found.", id);
        }

        RecommenderBuilder defaultInstance = recommenderListener.getInstance(instance.getKey());

        Recommender newInstance = defaultInstance.createInstance(id, param);
        newInstance.setState(RecommenderInformation.RecommenderState.TRAINING);

        Thread thread = new Thread() {
            public void run() {
                try {
                    newInstance.setDataSourceId(dataSource.getSourceId());
                    newInstance.setDataSet(dataSource.getDataSet());
                    newInstance.train();
                    newInstance.setState(RecommenderInformation.RecommenderState.READY);
                    recommender.put(id, newInstance);

                    threads.remove(newInstance.getId());
                } catch (BaseException e) {
                    e.printStackTrace();
                }
            }
        };

        threads.put(newInstance.getId(), thread);
        thread.start();

        return newInstance;
    }

    @Override
    public Message deleteRecommender(String id) {
        if (threads.containsKey(id)) {
            threads.get(id).interrupt();
            threads.remove(id);
        }

        if (recommender.containsKey(id)) {
            recommender.remove(id);
        }

        return new Message("Successful deleted.", String.format("The recommender with the id '%s' was successfully deleted", id), Message.Status.INFO);
    }
}
