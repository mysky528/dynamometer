/**
 * Copyright 2017 LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */
package com.linkedin.dynamometer.workloadgenerator.audit;

import com.google.common.base.Function;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;


/**
 * This {@link AuditCommandParser} is used to read commands assuming that the
 * input was generated by a Hive query storing uncompressed output files, in
 * which fields should be separated by the start-of-heading (U+0001) character.
 * The fields available should be, in order:
 * <pre>
 *   relativeTimestampMs,ugi,command,src,dest,sourceIP
 * </pre>
 * Where relativeTimestampMs represents the time elapsed between the start of
 * the audit log and the occurrence of the audit event. Assuming your audit
 * logs are available in Hive, this can be generated with a query looking like:
 * <pre>{@code
 *   INSERT OVERWRITE DIRECTORY '${outputPath}'
 *   SELECT (timestamp - ${startTimestamp} AS relativeTimestamp, ugi, cmd, src, dst, ip
 *   FROM '${auditLogTableLocation}'
 *   WHERE timestamp >= ${startTimestamp} AND timestamp < ${endTimestamp}
 *   DISTRIBUTE BY src
 *   SORT BY relativeTimestamp ASC;
 * }</pre>
 * Note that the sorting step is important; events in each distinct file must be in
 * time-ascending order.
 */
public class AuditLogHiveTableParser implements AuditCommandParser {

  private static final String FIELD_SEPARATOR = "\u0001";

  @Override
  public void initialize(Configuration conf) throws IOException {
    // Nothing to be done
  }

  @Override
  public AuditReplayCommand parse(Text inputLine, Function<Long, Long> relativeToAbsolute) throws IOException {
    String[] fields = inputLine.toString().split(FIELD_SEPARATOR);
    long absoluteTimestamp = relativeToAbsolute.apply(Long.parseLong(fields[0]));
    return new AuditReplayCommand(absoluteTimestamp, fields[1], fields[2], fields[3], fields[4], fields[5]);
  }

}
