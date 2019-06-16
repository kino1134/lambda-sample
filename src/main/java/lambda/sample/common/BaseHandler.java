package lambda.sample.common;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.ObjectError;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.annotation.Validated;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class BaseHandler<I extends SmartValidator> implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    /** メッセージファイルを取得 */
    private static LocalValidatorFactoryBean localValidatorFactoryBean;
    static {
        if (localValidatorFactoryBean == null) {
            ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
            messageSource.addBasenames("messages");
            messageSource.setDefaultEncoding("UTF-8");

            LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
            bean.setValidationMessageSource(messageSource);
            bean.afterPropertiesSet();

            localValidatorFactoryBean = bean;
        }
    }

    /**
     * API Gatewayからのエントリポイント
     */
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            // リクエストオブジェクトを準備する
            I target = getTarget();
            BindingResult errors = bind(request, target, getValidationGroups(target.getClass()));

            // 本処理を実行する
            Result<?> result = action(target, errors, context);

            // 結果をマッピングする
            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setStatusCode(result.statusCode);
            response.setHeaders(result.headers);
            response.setIsBase64Encoded(false);
            response.setBody(new ObjectMapper().writeValueAsString(result.entity)); // TODO

            return response;
        } catch (Exception e) {
            // TODO
            e.printStackTrace();

            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setStatusCode(500);
            response.setBody("{\"message\": \"" + e.getMessage() + "\"}");

            return response;
        }
    }

    /**
     * バリデーションエラーが発生したときの結果オブジェクトを取得する
     * @param errors
     * @return
     */
    protected Result<List<ObjectError>> bad(BindingResult errors) {
        Result<List<ObjectError>> result = new Result<>();

        result.statusCode = 400;
        result.entity = errors.getAllErrors(); // TODO

        return result;
    }

    /**
     * 正常終了したときの結果オブジェクトを取得する
     * @param <T>
     * @param entity
     * @return
     */
    protected <T> Result<T> ok(T entity) {
        return ok(entity, new LinkedHashMap<>());
    }

    /**
     * 正常終了したときの結果オブジェクトを取得する
     * @param <T>
     * @param entity
     * @param headers
     * @return
     */
    protected <T> Result<T> ok(T entity, Map<String, String> headers) {
        Result<T> result = new Result<>();

        result.statusCode = 200;
        result.headers.putAll(headers);
        result.entity = entity;

        return result;
    }

    /**
     * 本処理へ渡すリクエストオブジェクトを作成する
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @SuppressWarnings("unchecked")
    private I getTarget() throws InstantiationException, IllegalAccessException {
        Class<?> clazz = this.getClass();
        ParameterizedType type = (ParameterizedType)clazz.getGenericSuperclass();
        Type[] actualTypeArguments = type.getActualTypeArguments();
        Class<?> entityClass = (Class<?>)actualTypeArguments[0];

         return (I) entityClass.newInstance();
    }

    /**
     * 本処理のバリデーショングループを取得する
     * @param clazz
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    private Object[] getValidationGroups(Class<?> clazz) throws NoSuchMethodException, SecurityException {
        Method m = this.getClass().getMethod("action", clazz, BindingResult.class, Context.class);
        Validated validated = m.getAnnotation(Validated.class);

        if (validated != null) {
            return validated.value();
        } else {
            return null;
        }
    }

    /**
     * リクエストボディのJSONをMapに変換する
     * @param jsonStr
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    private Map<?, ?> getBodyMap(String jsonStr) throws JsonParseException, JsonMappingException, IOException {
        if (jsonStr == null || jsonStr.length() == 0) {
            return new LinkedHashMap<>();
        }

        // TODO
        return new ObjectMapper().readValue(jsonStr, new TypeReference<Map<?, ?>>() {
        });
    }

    /**
     * リクエスト内容をオブジェクトに設定して、バリデーションを行う
     * @param request
     * @param target
     * @param groups
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    private BindingResult bind(APIGatewayProxyRequestEvent request, I target, Object[] groups)
            throws JsonParseException, JsonMappingException, IOException {
        DataBinder binder = new DataBinder(target, target.getClass().getSimpleName());

        binder.addValidators(localValidatorFactoryBean);
        binder.addValidators(target);

        binder.bind(new MutablePropertyValues()
                .addPropertyValues(request.getPathParameters())
                .addPropertyValues(request.getQueryStringParameters())
                .addPropertyValues(getBodyMap(request.getBody())));

        binder.validate(groups);

        return binder.getBindingResult();
    }

    /**
     * 本処理
     * @param request リクエストパラメータ
     * @param errors リクエストのバリデーションエラー
     * @param context Lambdaのコンテキスト
     * @return 本処理のレスポンス
     */
    public abstract Result<?> action(I request, BindingResult errors, Context context) throws Exception;

}
