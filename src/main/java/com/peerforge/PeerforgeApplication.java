package com.peerforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PeerforgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PeerforgeApplication.class, args);
	}

}
