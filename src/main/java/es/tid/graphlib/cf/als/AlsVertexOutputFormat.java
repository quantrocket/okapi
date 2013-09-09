package es.tid.graphlib.cf.als;

import java.io.IOException;

import org.apache.giraph.graph.Vertex;
import org.apache.giraph.io.formats.TextVertexOutputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 * Simple text-based {@link org.apache.giraph.io.EdgeInputFormat} for graphs
 * with int ids.
 *
 * Each line consists of: vertex id, vertex value and option edge value
 */
public class AlsVertexOutputFormat extends
  TextVertexOutputFormat<Text, AlsVertexValue,
  DoubleWritable> {

  /** Specify the output delimiter. */
  public static final String LINE_TOKENIZE_VALUE = "output.delimiter";
  /** Default output delimiter. */
  public static final String LINE_TOKENIZE_VALUE_DEFAULT = "   ";
  /**
   * Create vertex writer.
   * @param context Context
   * @return a vertex writer
   */
  public final TextVertexWriter createVertexWriter(
    final TaskAttemptContext context) {
    return new AlsVertexWriter();
  }

  /**
   * A vertex writer. It prints:
   * - Int Vertex Id
   * - Double Array Vertex Value
   */
  protected class AlsVertexWriter
      extends TextVertexWriterToEachLine {
    /** Saved delimiter. */
    private String delimiter;

    @Override
    public final void initialize(final TaskAttemptContext context)
      throws IOException, InterruptedException {
      super.initialize(context);
      Configuration conf = context.getConfiguration();
      delimiter = conf
        .get(LINE_TOKENIZE_VALUE, LINE_TOKENIZE_VALUE_DEFAULT);
    }

    @Override
    protected final Text convertVertexToLine(
      final Vertex<Text, AlsVertexValue, DoubleWritable>vertex)
      throws IOException {
      boolean isErrorFlag = getContext().getConfiguration().getBoolean(
        "als.print.error", false);
      boolean isUpdatesFlag = getContext().getConfiguration().getBoolean(
        "als.print.updates", false);
      boolean isMessagesFlag = getContext().getConfiguration().getBoolean(
        "als.print.messages", false);

      String type = "";
      if (((Als) vertex).isItem()) {
        type = "item";
      } else {
        type = "user";
      }
      String id = vertex.getId().toString();
      String value = vertex.getValue().getLatentVector().toString();
      String error = null;
      String updates = null;
      String messages = null;
      Text line = new Text(type + delimiter + id + delimiter + value);

      if (isErrorFlag) {
        error = Double.toString(Math.abs(((Als) vertex).returnHaltFactor()));
        line.append(delimiter.getBytes(), 0, delimiter.length());
        line.append(error.getBytes(), 0, error.length());
      }
      if (isUpdatesFlag) {
        updates = Integer.toString(((Als) vertex).getUpdates()); // .toString();
        line.append(delimiter.getBytes(), 0, delimiter.length());
        line.append(updates.getBytes(), 0, updates.length());
      }
      if (isMessagesFlag) {
        messages = Integer.toString(((Als) vertex).getMessages());
        line.append(delimiter.getBytes(), 0, delimiter.length());
        line.append(messages.getBytes(), 0, messages.length());
      }
      return new Text(line);
    }
  }
}
