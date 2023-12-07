package io.github.pavansharma36.workflow.mongodb.helper;

import io.github.pavansharma36.workflow.api.model.ManagerInfo;
import io.github.pavansharma36.workflow.api.serde.Serde;
import java.util.HashMap;
import java.util.Map;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistry;

/**
 * Helper class to get codec.
 */
public class SerdeCodecProvider implements CodecRegistry {

  private final CodecRegistry delegation;
  private final Map<Class<?>, Codec<?>> codecs;

  /**
   * required args.
   *
   * @param delegation - to delegate
   * @param serde - serde
   */
  public SerdeCodecProvider(CodecRegistry delegation, Serde serde) {
    this.delegation = delegation;
    this.codecs = new HashMap<>();
    codecs.put(ManagerInfo.class, new SerdeCodec<>(serde, delegation,
        ManagerInfo.class));
  }

  @Override
  public <T> Codec<T> get(Class<T> clazz, CodecRegistry codecRegistry) {
    if (codecs.containsKey(clazz)) {
      return (Codec<T>) codecs.get(clazz);
    } else {
      return codecRegistry.get(clazz);
    }
  }

  @Override
  public <T> Codec<T> get(Class<T> clazz) {
    return get(clazz, delegation);
  }

}
