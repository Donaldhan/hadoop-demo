/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.story.offline.mapreduce;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * hdfs://pseduoDisHadoop:9000/user/donaldhan/input
 * hdfs://pseduoDisHadoop:9000/user/donaldhan/output
 */
@Slf4j
public class WordCountEx {
    private static final String INPUT_DIR = "hdfs://pseduoDisHadoop:9000/user/donaldhan/input/ip";
    private static final String OUTPUT_DIR = "hdfs://pseduoDisHadoop:9000/user/donaldhan/output/ip";
    /**
     *
     */
    public static class TokenizerMapper
            extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        @Override
        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString(), ",");
            while (itr.hasMoreTokens()) {
                word.set(itr.nextToken());
                context.write(word, one);
            }
        }
    }

    /**
     *
     */
    public static class IntSumReducer
            extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        @Override
        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

    /**
     * @param conf
     * @param path
     * @throws IOException
     */
    public  static void readFile(Configuration conf,String path) throws IOException {
        FSDataInputStream inStream = null;
        try {
            FileSystem fs = FileSystem.get(conf);
            inStream = fs.open(new Path(path));
            String data = "";
            do {
                data = inStream.readUTF();
                log.info(data);
            }
            while(StringUtils.isBlank(data));
        } catch (IOException e) {
            log.error("readFile error", e);
        } finally {
            assert inStream != null;
            inStream.close();
        }

    }
    /**
     * @param conf
     * @param path
     * @param content
     * @throws IOException
     */
    public static void writeFile(Configuration conf,String path, String content) throws IOException {
        FSDataOutputStream outStream = null;
        try {
            FileSystem fs = FileSystem.get(conf);
            outStream = fs.create(new Path(path));
            outStream.writeUTF(content);
        } catch (IOException e) {
            log.error("writeFile error, content:{}", content, e);
        } finally {
            if (outStream != null) {
                outStream.close();
            }
        }

    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        System.setProperty("hadoop.home.dir", "F:\\Hadoop\\hadoop-2.7.1");
 /*   conf.set("mapreduce.framework.name", "yarn");
    conf.set("fs.default.name", "hdfs://192.168.5.130:9000");
    //跨平台提交
    conf.set("mapreduce.app-submission.cross-platform", "true");*/

   /*//用maven打包程序，运行完的jar放在一个固定的路径下，然后在程序中设置
    conf.set("mapred.jar","D:\\java\\mapreduce\\target\\mapreduce-1.0-SNAPSHOT-jar-with-dependencies.jar");*/

   /* administrator没有权限访问集群上hadoop中的/tmp目录000000000000000000000
    这是由于本机的administrator没有操作hadoop目录的权限，需要配置hadoop_name，为方便起见就不在环境变量里面配置，直接在程序中加入：*/

//    System.setProperty("HADOOP_USER_NAME", "donaldhan");
        writeFile(conf,INPUT_DIR+"access-2019-03-25.txt","192.168.32.126,192.168.32.128 \\n");
        writeFile(conf,INPUT_DIR+"access-2019-03-24.txt","192.168.32.126,192.168.32.127 \\n");
        log.info("write done...");
        String[] inputPath = new String[]{INPUT_DIR};
        Job job = Job.getInstance(conf, "word count");
        job.setJarByClass(WordCountEx.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        for (int i = 0; i < inputPath.length - 1; ++i) {
            FileInputFormat.addInputPath(job, new Path(inputPath[i]));
        }
        FileOutputFormat.setOutputPath(job,
                new Path(OUTPUT_DIR+"result.text"));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
        readFile(conf, OUTPUT_DIR+"result.text");
    }
}
