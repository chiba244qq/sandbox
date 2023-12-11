import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.SocketException;

public class WebClientConnectionResetTest {

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        // カスタム Dispatcher を設定して、接続リセットをシミュレート
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public okhttp3.mockwebserver.MockResponse dispatch(okhttp3.mockwebserver.RecordedRequest recordedRequest) {
                return new okhttp3.mockwebserver.MockResponse()
                        .setSocketPolicy(SocketPolicy.DISCONNECT_AT_START); // 接続開始時に切断
            }
        });
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testWebClientConnectionReset() {
        // WebClient のインスタンスを作成
        WebClient webClient = WebClient.create(mockWebServer.url("/").toString());

        // WebClient を使用してリクエストを行い、エラーを検証
        Mono<String> response = webClient.get()
                .uri("/")
                .retrieve()
                .bodyToMono(String.class);

        // Connection Reset by Peer が発生することを検証
        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable instanceof SocketException
                        && throwable.getMessage().contains("Connection reset"))
                .verify();
    }
}
