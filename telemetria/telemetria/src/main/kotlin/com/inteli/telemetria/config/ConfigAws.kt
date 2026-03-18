package com.inteli.telemetria.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsClient

@Configuration
class AwsConfig {

    @Value("\${spring.cloud.aws.credentials.access-key}")
    lateinit var accessKey: String

    @Value("\${spring.cloud.aws.credentials.secret-key}")
    lateinit var secretKey: String

    @Value("\${spring.cloud.aws.region.static}")
    lateinit var region: String

    @Bean
    fun sqsClient(): SqsClient {
        val credentials = AwsBasicCredentials.create(accessKey, secretKey)

        return SqsClient.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build()
    }
}