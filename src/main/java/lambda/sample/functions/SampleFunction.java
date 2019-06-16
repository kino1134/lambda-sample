package lambda.sample.functions;

import org.springframework.validation.BindingResult;

import com.amazonaws.services.lambda.runtime.Context;

import lambda.sample.common.BaseHandler;
import lambda.sample.common.Result;
import lambda.sample.models.SampleInput;
import lambda.sample.models.SampleOutput;
import lambda.sample.services.SampleService;

public class SampleFunction extends BaseHandler<SampleInput> {

    @Override
    //@Validated({RequestHandler.class, Default.class})
    public Result<?> action(SampleInput request, BindingResult errors, Context context) {
        if (errors.hasErrors()) {
            return bad(errors);
        }

        SampleOutput res = new SampleService().execute(request);

        return ok(res);
    }



}
