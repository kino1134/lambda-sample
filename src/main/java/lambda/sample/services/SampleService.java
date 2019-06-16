package lambda.sample.services;

import java.util.Date;

import lambda.sample.models.SampleInput;
import lambda.sample.models.SampleOutput;

public class SampleService {

    public SampleOutput execute(SampleInput request) {
        SampleOutput res = new SampleOutput();
        res.test = request.test;
        res.date = new Date();

        return res;
    }

}
