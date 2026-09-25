package com.petstore.api.support;

import com.petstore.api.config.Config;
import io.restassured.response.Response;
import org.awaitility.core.ConditionTimeoutException;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.awaitility.Awaitility.await;

public final class Eventually {

    private Eventually() {
    }

    public static Response responseWithStatus(Supplier<Response> request, int expectedStatus, String description) {
        return response(request, r -> r.statusCode() == expectedStatus, description + " (HTTP " + expectedStatus + ")");
    }

    public static Response response(Supplier<Response> request, Predicate<Response> condition, String description) {
        AtomicReference<Response> last = new AtomicReference<>();
        try {
            await(description)
                    .atMost(Config.consistencyTimeout())
                    .pollInterval(Config.consistencyPollInterval())
                    .until(() -> {
                        Response response = request.get();
                        last.set(response);
                        return condition.test(response);
                    });
        } catch (ConditionTimeoutException e) {
            Response response = last.get();
            String lastSeen = response == null ? "no response"
                    : "HTTP " + response.statusCode() + " " + response.asString();
            throw new AssertionError("Timed out after " + Config.consistencyTimeout().toSeconds()
                    + "s waiting for: " + description + ". Last response: " + lastSeen, e);
        }
        return last.get();
    }
}
