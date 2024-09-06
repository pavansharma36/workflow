package io.github.pavansharma36.workflow.jackson.adapter;

import io.github.pavansharma36.workflow.api.adapter.Adapter;
import io.github.pavansharma36.workflow.api.adapter.builder.BaseAdapterBuilder;
import io.github.pavansharma36.workflow.api.serde.Serde;
import io.github.pavansharma36.workflow.jackson.serde.JacksonSerde;

public abstract class BaseSerdeAdapterBuilder<T extends BaseSerdeAdapterBuilder<T, A>, A extends Adapter>
    extends BaseAdapterBuilder<T, A> {
    protected Serde serde;

    public T withSerde(Serde serde) {
        this.serde = serde;
        return (T) this;
    }

    @Override
    protected void validate() {
        super.validate();
        if (serde == null) {
            serde = JacksonSerde.getInstance();
        }
    }
}
