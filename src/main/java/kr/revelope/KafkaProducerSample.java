package kr.revelope;

import org.apache.kafka.clients.producer.*;

import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Future;

public class KafkaProducerSample {
	public static void main(String[] args) {
		Properties kafkaProperties = new Properties();
		kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "");
		kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		kafkaProperties.put(ProducerConfig.ACKS_CONFIG, "all");

		sendSync(kafkaProperties);
		sendAsync(kafkaProperties);
		sendAsyncWithKey(kafkaProperties);
		sendAsyncWithKeyAndPartition(kafkaProperties);
	}

	public static void sendSync(Properties kafkaProperties) {
		Producer<String, String> producer = new KafkaProducer<>(kafkaProperties);

		for (int i = 0; i < 100; i++) {
			Future<RecordMetadata> future = producer.send(new ProducerRecord<>("test", "test" + i));
			try {
				RecordMetadata recordMetadata = future.get();
				System.out.println(String.format("[%d/%d]%s", recordMetadata.partition(), recordMetadata.offset(), recordMetadata.topic()));
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}

		producer.close();
	}

	public static void sendAsync(Properties kafkaProperties) {
		Producer<String, String> producer = new KafkaProducer<>(kafkaProperties);

		for (int i = 0; i < 10000; i++) {
			producer.send(new ProducerRecord<>("test", "test" + i), (recordMetadata, e) -> {
				if (Objects.nonNull(e)) {
					System.out.println(e.getMessage());
					e.printStackTrace();
					return;
				}

				System.out.println(String.format("[%d/%d]%s", recordMetadata.partition(), recordMetadata.offset(), recordMetadata.topic()));
			});
		}

		producer.close();
	}

	/**
	 * 키가 가지는 의미
	 * 1. 메세지를 식별하는 추가 정보
	 * 2. 파티션을 결정
	 * > 같은 키를 가지면 무조건 같은 파티션에 할당됨
	 * > 같은 파티션에 있다고 같은 키는 아님
	 */
	public static void sendAsyncWithKey(Properties kafkaProperties) {
		Producer<String, String> producer = new KafkaProducer<>(kafkaProperties);

		for (int i = 0; i < 10000; i++) {
			producer.send(new ProducerRecord<>("test", "AA01", "test" + i), (recordMetadata, e) -> {
				if (Objects.nonNull(e)) {
					System.out.println(e.getMessage());
					e.printStackTrace();
					return;
				}

				System.out.println(String.format("[%d/%d]%s", recordMetadata.partition(), recordMetadata.offset(), recordMetadata.topic()));
			});
		}

		producer.close();
	}

	public static void sendAsyncWithKeyAndPartition(Properties kafkaProperties) {
		Producer<String, String> producer = new KafkaProducer<>(kafkaProperties);

		for (int i = 0; i < 10000; i++) {
			producer.send(new ProducerRecord<>("test", 1, "AA01", "test" + i), (recordMetadata, e) -> {
				if (Objects.nonNull(e)) {
					System.out.println(e.getMessage());
					e.printStackTrace();
					return;
				}

				System.out.println(String.format("[%d/%d]%s", recordMetadata.partition(), recordMetadata.offset(), recordMetadata.topic()));
			});
		}

		producer.close();
	}
}
