package com.recalot.model.experiments.access;

import com.recalot.common.Helper;
import com.recalot.common.communication.DataSet;
import com.recalot.common.configuration.ConfigurationItem;
import com.recalot.common.exceptions.AlreadyExistsException;
import com.recalot.common.exceptions.BaseException;
import com.recalot.common.exceptions.MissingArgumentException;
import com.recalot.common.exceptions.NotFoundException;
import com.recalot.common.communication.Message;
import com.recalot.common.interfaces.model.data.DataSource;
import com.recalot.common.interfaces.model.experiment.DataSplitter;
import com.recalot.common.interfaces.model.experiment.Experiment;
import com.recalot.common.interfaces.model.experiment.Metric;
import com.recalot.common.interfaces.model.rec.Recommender;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by matthaeus.schmedding on 16.04.2015.
 */
public class ExperimentAccess implements com.recalot.common.interfaces.model.experiment.ExperimentAccess {

    private final BundleContext context;

    private final ConcurrentHashMap<String, Experiment> experiments;
    private final ConcurrentHashMap<String, Thread> threads;

    public ExperimentAccess(BundleContext context) {
        this.context = context;
        this.experiments = new ConcurrentHashMap<>();
        this.threads = new ConcurrentHashMap<>();
    }

    @Override
    public Experiment getExperiment(String id) throws BaseException {
        // Lock list and return data source object.
        synchronized (experiments) {
            if (experiments.containsKey(id)) {
                return experiments.get(id);
            }
        }

        throw new NotFoundException(String.format("Experiment with id %s not found.", id));
    }

    @Override
    public Message deleteExperiment(String id) throws BaseException {
        // Lock list and add data source object.
        synchronized (experiments) {
            if (experiments.containsKey(id)) {
                experiments.remove(id);

                if (threads.containsKey(id)) {
                    threads.get(id).interrupt();
                    threads.remove(id);
                }
                return new Message("Delete successful", String.format("Experiment with id %s successful deleted.", id), Message.Status.INFO);
            }
        }

        throw new NotFoundException(String.format("Experiment with id %s not found.", id));
    }

    @Override
    public List<Experiment> getExperiments() throws BaseException {
        return new ArrayList<>(experiments.values());
    }

    @Override
    public Experiment createExperiment(Recommender[] recommender, DataSource dataSource, DataSplitter splitter, HashMap<String, Metric[]> metrics, Map<String, String> param) throws BaseException {

        String id = param.get(Helper.Keys.ExperimentId);
        if (id == null) id = UUID.randomUUID().toString();

        if (threads.containsKey(id) || experiments.containsKey(id))
            throw new AlreadyExistsException("An experiment with the id %s already exists. Please first delete the experiment.", id);
        if (param.get(Helper.Keys.MetricIDs) == null)
            throw new MissingArgumentException("The argument %s is missing.", Helper.Keys.MetricIDs);

        Experiment experiment = new com.recalot.common.impl.experiment.Experiment(id, dataSource, splitter, recommender, metrics, param);

        Thread thread = new Thread() {
            public void run() {
                experiment.run();

                threads.remove(experiment.getId());
            }
        };

        threads.put(id, thread);
        thread.start();

        experiments.put(experiment.getId(), experiment);

        return experiment;
    }

    @Override
    public String getKey() {
        return "experiment-access";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
