package window;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @description:
 * @create: 2020/4/12
 * @author: altenchen
 */
public class KeyedProcessFunctionMonitor {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment senv = StreamExecutionEnvironment.getExecutionEnvironment();
        senv.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        senv.setParallelism(1);

        DataStream<String> sourceDS = senv.socketTextStream("localhost", 9000)
                .filter(new FilterFunction<String>() {
                    public boolean filter(String line) throws Exception {
                        if (null == line || "".equals(line)) {
                            return false;
                        }
                        String[] lines = line.split(",");
                        if (lines.length != 3) {
                            return false;
                        }
                        return true;
                    }
                });

        DataStream<String> warningDS = sourceDS.map(new MapFunction<String, MessageInfo>() {
            public MessageInfo map(String line) throws Exception {
                String[] lines = line.split(",");
                return new MessageInfo(lines[0], lines[1], lines[2]);
            }
        }).keyBy(new KeySelector<MessageInfo, String>() {
            public String getKey(MessageInfo value) throws Exception {
                return value.hostname;
            }
        }).process(new MyKeyedProcessFunction());
        /*打印报警信息*/
        warningDS.print();

        senv.execute();



    }


}
