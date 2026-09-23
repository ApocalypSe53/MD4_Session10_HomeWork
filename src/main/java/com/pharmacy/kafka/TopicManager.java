package com.pharmacy.kafka;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Quản lý topic thông qua Kafka AdminClient: tạo, liệt kê, mô tả chi tiết partition.
 */
public class TopicManager implements AutoCloseable {

    /** Chỉ có 1 broker nên replication factor = 1. */
    private static final short REPLICATION_FACTOR = 1;

    private final Admin admin;

    public TopicManager(String bootstrapServers) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 10_000);
        props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 15_000);
        this.admin = Admin.create(props);
    }

    /** In thông tin các broker trong cluster. */
    public void printClusterInfo() throws ExecutionException, InterruptedException {
        var cluster = admin.describeCluster();
        System.out.println("Cluster ID : " + cluster.clusterId().get());
        System.out.println("Controller : " + formatNode(cluster.controller().get()));
        System.out.println("Brokers    :");
        for (Node node : cluster.nodes().get()) {
            System.out.println("  - " + formatNode(node));
        }
    }

    /**
     * Tạo các topic còn thiếu. Nếu topic đã tồn tại nhưng ít partition hơn yêu cầu thì tăng thêm
     * (Kafka không cho phép giảm số partition).
     */
    public void createTopics() throws ExecutionException, InterruptedException {
        Set<String> existing = admin.listTopics().names().get();

        var toCreate = Arrays.stream(PharmacyTopic.values())
                .filter(t -> !existing.contains(t.topicName()))
                .map(t -> new NewTopic(t.topicName(), t.partitions(), REPLICATION_FACTOR))
                .toList();

        if (!toCreate.isEmpty()) {
            admin.createTopics(toCreate).all().get();
            toCreate.forEach(t -> System.out.printf("[CREATED] %-25s partitions=%d%n",
                    t.name(), t.numPartitions()));
        }

        var alreadyExisting = Arrays.stream(PharmacyTopic.values())
                .filter(t -> existing.contains(t.topicName()))
                .toList();
        if (alreadyExisting.isEmpty()) {
            return;
        }

        Map<String, TopicDescription> descriptions = admin.describeTopics(
                alreadyExisting.stream().map(PharmacyTopic::topicName).toList()).allTopicNames().get();

        for (PharmacyTopic topic : alreadyExisting) {
            int current = descriptions.get(topic.topicName()).partitions().size();
            if (current < topic.partitions()) {
                admin.createPartitions(Map.of(topic.topicName(),
                        NewPartitions.increaseTo(topic.partitions()))).all().get();
                System.out.printf("[UPDATED] %-25s partitions %d -> %d%n",
                        topic.topicName(), current, topic.partitions());
            } else if (current > topic.partitions()) {
                System.out.printf("[WARNING] %-25s có %d partition (> %d yêu cầu), Kafka không hỗ trợ giảm."
                        + " Hãy xóa topic và tạo lại.%n", topic.topicName(), current, topic.partitions());
            } else {
                System.out.printf("[EXISTS ] %-25s partitions=%d%n", topic.topicName(), current);
            }
        }
    }

    /** Liệt kê tất cả topic (bỏ qua topic nội bộ như __consumer_offsets). */
    public Set<String> listTopics() throws ExecutionException, InterruptedException {
        return admin.listTopics().names().get();
    }

    /** In chi tiết partition của các topic dược phẩm: leader, replicas, ISR. */
    public void describeTopics() throws ExecutionException, InterruptedException {
        var names = Arrays.stream(PharmacyTopic.values()).map(PharmacyTopic::topicName).toList();
        Map<String, TopicDescription> descriptions = admin.describeTopics(names).allTopicNames().get();

        for (PharmacyTopic topic : PharmacyTopic.values()) {
            TopicDescription desc = descriptions.get(topic.topicName());
            int actual = desc.partitions().size();
            String status = actual == topic.partitions() ? "OK" : "SAI (mong đợi " + topic.partitions() + ")";

            System.out.printf("%nTopic: %s | TopicId: %s | PartitionCount: %d [%s]%n",
                    desc.name(), desc.topicId(), actual, status);
            for (TopicPartitionInfo p : desc.partitions()) {
                System.out.printf("   Partition: %d  Leader: %s  Replicas: %s  Isr: %s%n",
                        p.partition(),
                        p.leader() == null ? "none" : p.leader().id(),
                        p.replicas().stream().map(n -> String.valueOf(n.id())).collect(Collectors.joining(",")),
                        p.isr().stream().map(n -> String.valueOf(n.id())).collect(Collectors.joining(",")));
            }
        }
    }

    private static String formatNode(Node node) {
        return "id=" + node.id() + " (" + node.host() + ":" + node.port() + ")";
    }

    @Override
    public void close() {
        admin.close();
    }
}
