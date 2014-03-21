package at.ac.tuwien.dsg.comot.common.model;

import java.util.Map;

/**
 * Created by omoser on 3/1/14.
 */
public interface CloudEntity {

    String getId();

    String getName();

    String getType();

    String getDescription();

    Map<String, CloudEntity> getContext();
}
