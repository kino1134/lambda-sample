package lambda.sample;

import static org.mockito.Mockito.*;

import org.junit.Test;
import org.springframework.validation.BindingResult;

import com.amazonaws.services.lambda.runtime.Context;

import lambda.sample.common.Result;
import lambda.sample.functions.SampleFunction;
import lambda.sample.models.SampleInput;

public class SampleFunctionTest {

    @Test
    public void test() {
        BindingResult errors = mock(BindingResult.class);
        Context context = mock(Context.class);

        SampleInput input = new SampleInput();
        input.date = "2019-07-01";

        Result<?> output = new SampleFunction().action(input, errors, context);
        System.out.println(output);
    }

}
