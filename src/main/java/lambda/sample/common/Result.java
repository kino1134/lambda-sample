package lambda.sample.common;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * 本処理が返す結果オブジェクト
 * @param <R>
 */
@Getter
@Setter
public class Result<R> {
    /** API Gatewayが返すステータスコード */
    public int statusCode;

    /** API Gatewayで返すHTTPヘッダーに追加する */
    public Map<String, String> headers = new LinkedHashMap<>();

    /** 本処理自体の結果 */
    public R entity;

}
