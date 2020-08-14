package hadoop.job2;

import hadoop.BeerOrBrewery;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class BreweriesClassesReducer extends Reducer<IntWritable, BreweriesAndClasses, IntWritable, BreweriesAndClasses> {
    public void reduce(IntWritable key, Iterable<BreweriesAndClasses> values, Context context) throws IOException, InterruptedException {
        BreweriesAndClasses result = new BreweriesAndClasses();
        for(BreweriesAndClasses value: values){
            if(value.isClass()){
                result.setBeerClass(value.getBeerClass());
            }else {
                result.setBeerOrBrewery(value.getBeerOrBrewery());
            }
        }
        context.write(new IntWritable(result.getBeerOrBrewery().getBrewery().getId()),result);
    }
}
