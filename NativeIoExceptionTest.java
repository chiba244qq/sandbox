import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpClient;
import reactor.test.StepVerifier;

import java.net.ConnectException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebClientNativeIoExceptionTest {

    @Test
    public void testNativeIoException() {
        // モックの ConnectionProvider を作成
        ConnectionProvider provider = mock(ConnectionProvider.class);

        // TcpClient をモックして、ConnectException をスローするように設定
        TcpClient tcpClient = TcpClient.create(provider);
        TcpClient mockedTcpClient = mock(TcpClient.class);
        when(mockedTcpClient.connect()).thenAnswer(invocation -> Mono.error(new ConnectException("Forced connection failure")));

        // HttpClient をカスタマイズしてモックの TcpClient を使用
        HttpClient httpClient = HttpClient.from(mockedTcpClient);

        // WebClient をカスタマイズして HttpClient を使用
        WebClient webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        // WebClient を使用してリクエストを行い、エラーを検証
        Mono<String> response = webClient.get()
                .uri("http://localhost:8080")
                .retrieve()
                .bodyToMono(String.class);

        // ConnectException が発生することを検証
        StepVerifier.create(response)
                .expectError(ConnectException.class)
                .verify();
    }
}

