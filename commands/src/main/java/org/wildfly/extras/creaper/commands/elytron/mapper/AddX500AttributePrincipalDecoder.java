package org.wildfly.extras.creaper.commands.elytron.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public final class AddX500AttributePrincipalDecoder implements OnlineCommand {

    private final String name;
    private final String oid;
    private final String joiner;
    private final Integer startSegment;
    private final Integer maximumSegments;
    private final Boolean reverse;
    private final List<String> requiredOids;
    private final boolean replaceExisting;

    private AddX500AttributePrincipalDecoder(Builder builder) {
        this.name = builder.name;
        this.oid = builder.oid;
        this.joiner = builder.joiner;
        this.startSegment = builder.startSegment;
        this.maximumSegments = builder.maximumSegments;
        this.reverse = builder.reverse;
        this.requiredOids = builder.requiredOids;
        this.replaceExisting = builder.replaceExisting;
    }

    @Override
    public void apply(OnlineCommandContext ctx) throws Exception {
        Operations ops = new Operations(ctx.client);
        Address x500AttributePrincipalDecoderAddress = Address.subsystem("elytron")
                .and("x500-attribute-principal-decoder", name);
        if (replaceExisting) {
            ops.removeIfExists(x500AttributePrincipalDecoderAddress);
            new Administration(ctx.client).reloadIfRequired();
        }

        ops.add(x500AttributePrincipalDecoderAddress, Values.empty()
                .and("oid", oid)
                .andOptional("joiner", joiner)
                .andOptional("start-segment", startSegment)
                .andOptional("maximum-segments", maximumSegments)
                .andOptional("reverse", reverse)
                .andListOptional(String.class, "required-oids", requiredOids));

    }

    public static final class Builder {

        private final String name;
        private String oid;
        private String joiner;
        private Integer startSegment;
        private Integer maximumSegments;
        private Boolean reverse;
        private List<String> requiredOids;
        private boolean replaceExisting;

        public Builder(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Name of the x500-attribute-principal-decoder must be specified as non null value");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name of the x500-attribute-principal-decoder must not be empty value");
            }
            this.name = name;
        }

        public Builder oid(String oid) {
            this.oid = oid;
            return this;
        }

        public Builder joiner(String joiner) {
            this.joiner = joiner;
            return this;
        }

        public Builder startSegment(int startSegment) {
            this.startSegment = startSegment;
            return this;
        }

        public Builder maximumSegments(int maximumSegments) {
            this.maximumSegments = maximumSegments;
            return this;
        }

        public Builder reverse(boolean reverse) {
            this.reverse = reverse;
            return this;
        }

        public Builder addRequiredOids(String... requiredOids) {
            if (requiredOids == null) {
                throw new IllegalArgumentException("Required OIDs added to x500-attribute-principal-decoder must not be null");
            }
            if (this.requiredOids == null) {
                this.requiredOids = new ArrayList<String>();
            }

            Collections.addAll(this.requiredOids, requiredOids);
            return this;
        }

        public Builder replaceExisting() {
            this.replaceExisting = true;
            return this;
        }

        public AddX500AttributePrincipalDecoder build() {
            if (oid == null || oid.isEmpty()) {
                throw new IllegalArgumentException("Oid must not be null and must have a minimum length of 1 character");
            }

            return new AddX500AttributePrincipalDecoder(this);
        }
    }
}
