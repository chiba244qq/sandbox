import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class WebClientCustomExceptionExample {
    public static void main(String[] args) {
        WebClient webClient = WebClient.builder()
                .filter(throwCustomException())
                .baseUrl("http://localhost:8080")
                .build();

        Mono<String> response = webClient.post()
                .uri("/path")
                .bodyValue("request body")
                .retrieve()
                .bodyToMono(String.class);

        response.subscribe(
                result -> System.out.println("Result: " + result),
                error -> System.out.println("Error: " + error.getMessage())
        );
    }

    private static ExchangeFilterFunction throwCustomException() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            // ここで任意の例外をスロー
            throw new RuntimeException("Custom exception thrown!");
        });
    }
}

