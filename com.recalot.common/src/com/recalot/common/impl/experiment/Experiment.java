package com.recalot.common.impl.experiment;

import com.recalot.common.Helper;
import com.recalot.common.Parallel;
import com.recalot.common.communication.*;
import com.recalot.common.exceptions.BaseException;
import com.recalot.common.interfaces.model.data.DataSource;
import com.recalot.common.interfaces.model.experiment.DataSplitter;
import com.recalot.common.interfaces.model.experiment.ListMetric;
import com.recalot.common.interfaces.model.experiment.Metric;
import com.recalot.common.interfaces.model.experiment.RatingMetric;
import com.recalot.common.context.ContextProvider;
import com.recalot.common.interfaces.model.rec.Recommender;
import org.osgi.service.log.LogService;

import java.util.*;

/**
 * @author Matthäus Schmedding (info@recalot.com)
 */
public class Experiment extends com.recalot.common.interfaces.model.experiment.Experiment {

    private final HashMap<String, Metric[]> metrics;
    private final DataSource dataSource;
    private final DataSplitter splitter;
    private ContextProvider context;
    private Map<String, String> param;

    public Experiment(String id, DataSource source, DataSplitter splitter, Recommender[] recommenders, HashMap<String, Metric[]> metrics, ContextProvider context, Map<String, String> param) {
        this.context = context;
        this.param = param;

        this.id = id;
        this.recommenders = recommenders;

        recommenderIds = new String[this.recommenders.length];
        for (int i = 0; i < recommenders.length; i++) {
            recommenderIds[i] = recommenders[i].getId();
        }

        this.dataSourceId = source.getId();
        this.dataSource = source;
        this.splitter = splitter;
        this.metrics = metrics;
        this.result = new HashMap<>();
    }

    @Override
    public void run() {
        setState(ExperimentState.RUNNING);
        setPercentage(0);
        setInfo("Split data source");

//TODO add total time of experiment
        try {

            DataSet[] sets = splitter.split(dataSource);

            if (!param.containsKey(Helper.Keys.SplitType) || param.get(Helper.Keys.SplitType).equals("simple")) {
                //check test data set against train data set

                setInfo("Started training recommenders with first split");

                trainRecommenders(sets[0]);
                setPercentage(50);
                setInfo("Started testing of the recommenders with second split");
                performTest(recommenders, sets[1], 1.0 * 50 / recommenders.length);

            } else if (param.get(Helper.Keys.SplitType).equals("n-fold")) {

                // steps for completion:
                // train each recommender with each data set
                // test each trained recommender with the data that is not in the train data set
                double percentageSteps = 100.00 / (recommenders.length * sets.length  * 2);

                //do cross validation, train one set and test it with the users
                for (int i = 0; i < sets.length; i++) {
                    setInfo(String.format("Started training recommenders with split %s", i));

                    DataSet trainDataSet = FillableDataSet.createDataSet(dataSource.getDataSet(), sets[i].getInteractions());

                    trainRecommenders(trainDataSet);

                    setPercentage(getPercentage() + (recommenders.length * percentageSteps));

                    performTest(recommenders, sets[i], percentageSteps);
                }
            }
        } catch (BaseException e) {
            setInfo(e.getMessage());
        }

        setInfo("Done");
        setState(ExperimentState.FINISHED);
    }

    private void performTest(Recommender[] recommenders, DataSet test, double percentage) {
        //iterate over all recommenders
        for (Recommender r : recommenders) {

            setInfo(String.format("Evaluate trained recommender %s", r.getId()));

            try {
                User[] users = test.getUsers();
                DataSet t = test;
                //iterate over all users
                // make it parallel
                Parallel.For(Arrays.asList(users),
                        u -> {

                            Interaction[] userInteractions = new Interaction[0];

                            //get the interactions of the user in the test set
                            try {
                                userInteractions = test.getInteractions(u.getId());
                            } catch (BaseException e) {

                            }

                            //iterate over all metric for this recommender
                            for (Metric m : metrics.get(r.getId())) {

                                if (m instanceof RatingMetric) {
                                    for (Interaction interaction : userInteractions) {
                                        Integer value = Integer.parseInt(interaction.getValue());
                                        Double predict = r.predict(interaction.getUserId(), interaction.getItemId(), context);
                                        if (!predict.isNaN()) {
                                            ((RatingMetric) m).addRating(value, predict);
                                        }
                                    }
                                } else if (m instanceof ListMetric) {
                                    ArrayList<String> interactions = new ArrayList();
                                    for (Interaction interaction : userInteractions) {
                                        interactions.add(interaction.getItemId());
                                    }

                                    ArrayList<String> result1 = new ArrayList();

                                    RecommendationResult rr = r.recommend(u.getId());

                                    for (RecommendedItem item : rr.getItems()) {
                                        result1.add(item.getItemId());
                                    }

                                    ((ListMetric) m).addList(interactions, result1);
                                }
                            }
                        });
                setPercentage(getPercentage() + percentage);
            } catch (BaseException e) {
                e.printStackTrace();

                setInfo(e.getMessage());
            }


            //copy results to output
            for (String key : metrics.keySet()) {
                Metric[] ms = metrics.get(key);
                HashMap<String, Double> res = new HashMap<>();
                for (Metric m : ms) {
                    res.put(m.getId(), m.getResult());
                }

                this.result.put(key, res);
            }
        }
    }

    private void trainRecommenders(DataSet train) {

        Parallel.For(Arrays.asList(recommenders), r -> {
            try {
                r.setDataSet(train);

                r.train();
            } catch (Exception e) {
                e.printStackTrace();
                if (this.logger != null) {
                    this.logger.log(LogService.LOG_ERROR, String.format("The training of recommender %s failed. Exception message: %s", r.getId(), e.getMessage()));
                    setInfo(String.format("The training of recommender %s failed. Exception message: %s", r.getId(), e.getMessage()));
                }
            }
        });
    }
}
