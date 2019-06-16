package lambda.sample.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.validation.constraints.NotBlank;

import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SampleInput implements SmartValidator {
    @NotBlank
    public String test;

    @NotBlank
    public String date;

    @Override
    public boolean supports(Class<?> clazz) {
        return this.getClass().isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validate(target, errors, new Object[] {});
    }

    @Override
    public void validate(Object target, Errors errors, Object... validationHints) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy/MM/dd");
        try {
            if (date != null) {
                Date d = f.parse(date);
                if (d.after(new Date())) {
                    errors.rejectValue("date", "", "日付指定が正しくありません。");
                }
            }
        } catch (ParseException e) {
            errors.rejectValue("date", "", "日付指定が正しくありません。");
        }
    }
}
