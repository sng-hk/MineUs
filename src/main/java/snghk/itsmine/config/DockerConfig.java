package snghk.itsmine.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration // Spring Boot에게 설정 파일임을 알립니다.
public class DockerConfig {

    @Bean // Spring 애플리케이션 컨텍스트에 DockerClient 객체를 등록합니다.
    public DockerClient dockerClient() {
        // 1. Docker 데몬(소켓)에 연결하기 위한 설정 객체 생성
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                // Windows/Mac/Linux에서 기본 Docker 소켓 주소를 자동으로 찾도록 설정
                .build();

        // 2. HTTP 통신 클라이언트 설정 (우리가 추가한 httpclient5 사용)
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost()) // 1번에서 찾은 Docker 주소 사용
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        // 3. Docker 클라이언트(리모컨) 객체 생성 및 반환
        return DockerClientImpl.getInstance(config, httpClient);
    }
}