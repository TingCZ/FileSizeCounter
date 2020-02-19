import com.sun.org.apache.bcel.internal.generic.ARRAYLENGTH;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;


public class FileSizeCounter {
    private final static ForkJoinPool forkJoinPool = new ForkJoinPool();

    private static class FileSizeFinder extends RecursiveTask<Long>{
        final File file;

        public FileSizeFinder(File file){
            this.file = file;
        }

        @Override
        protected Long compute() {
            Long fileSize = 0l;

            if (file.isFile()){
                fileSize = file.length();
            }else{
                File[] children = file.listFiles();

                if(children != null){
                    List<ForkJoinTask<Long>> tasks = new ArrayList<>();

                    for(File child : children){
                        if (child.isFile()){
                            fileSize += child.length();
                        }
                        else{
                            tasks.add(new FileSizeFinder(child));
                        }
                    }

                    for (ForkJoinTask<Long> task : invokeAll(tasks)){
                        fileSize += task.join();
                    }

//                     fileSize += ForkJoinTask.invokeAll(tasks).stream()
//                            .mapToLong(ForkJoinTask::join)
//                            .sum();
                }
            }
            return fileSize;
        }

        public static void main(String[] args) {
            LocalDateTime localDateTime1 = LocalDateTime.now();
            System.out.println(localDateTime1);
            long totalSize = forkJoinPool.invoke(
                    new FileSizeFinder(new File("/Users/tingcheung")));

            System.out.println("File size = " + totalSize / (1000.0 * 1000.00 * 1000) + "GB");
            LocalDateTime localDateTime2 = LocalDateTime.now();
            System.out.println(localDateTime2);

            System.out.println(Duration.between(localDateTime1, localDateTime2).toMillis());


//            2020-02-07T14:39:31.250
//            File size = 4587.515097651GB
//            2020-02-07T14:39:41.752
        }
    }

}
