package com.recalot.common.interfaces.model.experiment;

import com.recalot.common.communication.DataSet;
import com.recalot.common.communication.Message;
import com.recalot.common.configuration.ConfigurationItem;
import com.recalot.common.exceptions.BaseException;
import com.recalot.common.communication.Service;
import com.recalot.common.interfaces.model.data.DataSource;
import com.recalot.common.interfaces.model.rec.Recommender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author matthaeus.schmedding
 */
public interface ExperimentAccess extends Service {
    public Experiment getExperiment(String id) throws BaseException;
    public Message deleteExperiment(String id)  throws BaseException;
    public List<Experiment> getExperiments()  throws BaseException;
    public Experiment createExperiment(Recommender[] recommender, DataSource source, DataSplitter splitter, HashMap<String, Metric[]> metrics, Map<String, String> param) throws BaseException;
}
