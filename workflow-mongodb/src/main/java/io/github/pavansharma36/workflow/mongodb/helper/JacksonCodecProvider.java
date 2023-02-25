package io.github.pavansharma36.workflow.mongodb.helper;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistry;

@RequiredArgsConstructor
public class JacksonCodecProvider implements CodecRegistry {

  private static final Map<Class<?>, Codec<?>> CODECS = new HashMap<>();

  private final CodecRegistry delegation;

  @Override
  public <T> Codec<T> get(Class<T> aClass, CodecRegistry codecRegistry) {
    if (CODECS.containsKey(aClass)) {
      return (Codec<T>) CODECS.get(aClass);
    } else {
      return codecRegistry.get(aClass);
    }
  }

  @Override
  public <T> Codec<T> get(Class<T> aClass) {
    return get(aClass, delegation);
  }
}
