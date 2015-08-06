package com.recalot.model.experiments.splitter;

import com.recalot.common.communication.DataSet;
import com.recalot.common.exceptions.BaseException;
import com.recalot.common.interfaces.model.data.DataSource;
import com.recalot.common.interfaces.model.experiment.DataSplitter;

import java.io.IOException;

/**
 * TODO: implement this class
 * Created by matthaeus.schmedding on 16.04.2015.
 */
public class TimeBasedDataSplitter extends DataSplitter {
    @Override
    public DataSet[] split(DataSource source) throws BaseException {
        return new DataSet[0];
    }

    @Override
    public String getKey() {
        return "time";
    }

    @Override
    public String getId() {
        return "time";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
