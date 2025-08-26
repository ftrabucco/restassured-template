import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ApiTest {

    @Test
    public void testGetEndpoint() {
        Response response = RestAssured.get("https://jsonplaceholder.typicode.com/posts/1");
        assertThat(response.getStatusCode(), equalTo(200));
        assertThat(response.jsonPath().getInt("id"), equalTo(1));
    }
    //

    @Test
    public void testGetEndpointGastos() {
        Response response = RestAssured.get("http://localhost:3030/api/gastos/all");
        assertThat(response.getStatusCode(), equalTo(200));
        //assertThat(response.jsonPath().getInt("id"), equalTo(1));
    }

}
