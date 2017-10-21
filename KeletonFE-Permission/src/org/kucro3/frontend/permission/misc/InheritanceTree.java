package org.kucro3.frontend.permission.misc;

import org.kucro3.util.Reference;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class InheritanceTree {
    private InheritanceTree(int height, InheritanceNode root)
    {
        this.height = height;
        this.root = root;
    }

    public void preorder(List<Text> dest, @Nullable BiFunction<Text, Builder.ComputeEntry, Text> weakener, boolean ignoreRoot)
    {
        preorder(height, dest, weakener, ignoreRoot);
    }

    public void preorder(List<Text> dest, @Nullable BiFunction<Text, Builder.ComputeEntry, Text> weakener)
    {
        preorder(height, dest, weakener);
    }

    public void preorder(int height, List<Text> dest, @Nullable BiFunction<Text, Builder.ComputeEntry, Text> weakener)
    {
        preorder(height, dest, weakener, false);
    }

    public void preorder(int height, List<Text> dest, @Nullable BiFunction<Text, Builder.ComputeEntry, Text> weakener, boolean ignoreRoot)
    {
        root.preorder(height, dest, weakener, ignoreRoot);
    }

    public int getHeight()
    {
        return height;
    }

    public InheritanceNode getRoot()
    {
        return root;
    }

    public static Builder builder()
    {
        return new Builder(null, false);
    }

    public static Builder builder(boolean computeWeaken)
    {
        return new Builder(null, computeWeaken);
    }

    private final int height;

    private final InheritanceNode root;

    public static abstract class TreeNode
    {
        public TreeNode(int height)
        {
            this.height = height;
        }

        public int getHeight()
        {
            return height;
        }

        private final int height;
    }

    public static final class InheritanceNode extends TreeNode
    {
        InheritanceNode()
        {
            this(1);
        }

        InheritanceNode(int height)
        {
            this(height, null);
        }

        InheritanceNode(int height, InheritanceNode parent)
        {
            super(height);
            this.parent = parent;
            this.children = new ArrayList<>();
        }

        InheritanceNode(InheritanceNode parent)
        {
            this(parent.getHeight() + 1, parent);
        }

        public Text getContent()
        {
            return content;
        }

        void setContent(Text content)
        {
            this.content = Objects.requireNonNull(content);
        }

        void setContextSymbol(@Nullable Text contextSymbol)
        {
            this.contextSymbol = contextSymbol;
        }

        void setInheritanceSymbol(@Nullable Text inheritanceSymbol)
        {
            this.inheritanceSymbol = inheritanceSymbol;
        }

        void setRawContent(String rawContent)
        {
            this.rawContent = Objects.requireNonNull(rawContent);
        }

        void setRawContexts(Set<Context> rawContexts)
        {
            this.rawContexts = Objects.requireNonNull(rawContexts);
        }

        public Text getContextSymbol()
        {
            return contextSymbol;
        }

        public Text getInheritanceSymbol()
        {
            return inheritanceSymbol;
        }

        public List<InheritanceNode> getChildren()
        {
            return Collections.unmodifiableList(children);
        }

        public InheritanceNode getParent()
        {
            return parent;
        }

        public String getRawContent()
        {
            return rawContent;
        }

        public Set<Context> getRawContexts()
        {
            return Collections.unmodifiableSet(rawContexts);
        }

        public boolean isWeaken()
        {
            return weaken;
        }

        public void setWeaken(boolean weaken)
        {
            this.weaken = weaken;
        }

        // DLR
        public void preorder(int height, List<Text> dest, @Nullable BiFunction<Text, Builder.ComputeEntry, Text> weakener, boolean ignoreRoot)
        {
            if(!ignoreRoot)
                dest.add(toText(weakener));

            if(this.getHeight() < height)
            for(InheritanceNode node : this.children)
                node.preorder(height, dest, weakener, false);
        }

        public Text toText(@Nullable BiFunction<Text, Builder.ComputeEntry, Text> weakener)
        {
            Text content = Objects.requireNonNull(this.content);
            Text contextSymbol = this.contextSymbol;
            Text inheritanceSymbol = this.inheritanceSymbol;

            if(weaken)
                if(weakener != null)
                    content = weakener.apply(content, new Builder.ComputeEntry(rawContent, rawContexts));

            Text.Builder builder = Text.builder();

            if(contextSymbol != null)
                builder.append(contextSymbol);

            if(inheritanceSymbol != null)
                builder.append(inheritanceSymbol);

            builder.append(content);

            return builder.build();
        }

        private final InheritanceNode parent;

        final ArrayList<InheritanceNode> children;

        private boolean weaken;

        private String rawContent;

        private Set<Context> rawContexts;

        private Text content;

        private Text contextSymbol;

        private Text inheritanceSymbol;
    }

    public static final class Builder
    {
        Builder(Builder parent, boolean computeWeaken)
        {
            this.parent = parent;
            this.computeWeaken = computeWeaken;
        }

        public Builder getParent()
        {
            return parent;
        }

        public Text getContent()
        {
            return content;
        }

        public Text getContextSymbol()
        {
            return contextSymbol;
        }

        public Text getInheritanceSymbol()
        {
            return inheritanceSymbol;
        }

        public String getRawContent()
        {
            return rawContent;
        }

        public Set<Context> getRawContexts()
        {
            return rawContexts;
        }

        public boolean isWeaken()
        {
            return weaken;
        }

        public Builder rawContent(String rawContent)
        {
            this.rawContent = rawContent;
            return this;
        }

        public Builder weaken(boolean weaken)
        {
            this.weaken = weaken;
            return this;
        }

        public Builder content(Text content)
        {
            this.content = content;
            return this;
        }

        public Builder contextSymbol(Text contextSymbol)
        {
            this.contextSymbol = contextSymbol;
            return this;
        }

        public Builder inheritanceSymbol(Text inheritanceSymbol)
        {
            this.inheritanceSymbol = inheritanceSymbol;
            return this;
        }

        public Builder rawContexts(Set<Context> contexts)
        {
            this.rawContexts = contexts;
            return this;
        }

        public boolean computeWeaken()
        {
            return computeWeaken;
        }

        public InheritanceTree build()
        {
            Reference<Integer> counter = new Reference<>(1);
            InheritanceNode root = this.buildTreeNode(counter, 1, null);
            return new InheritanceTree(counter.get(), root);
        }

        public Builder child()
        {
            Builder builder = new Builder(this, computeWeaken);
            builder.hashSet = hashSet;
            children.add(builder);
            return builder;
        }

        public Builder escape()
        {
            return Objects.requireNonNull(getParent());
        }

        InheritanceNode buildTreeNode(Reference<Integer> counter, int height, InheritanceNode parent)
        {
            InheritanceNode node = this.buildNode(height, parent);
            counter.set(Math.max(counter.get(), height));
            height++;
            for(Builder child : children)
                node.children.add(child.buildTreeNode(counter, height, node));
            return node;
        }

        InheritanceNode buildNode(int height, InheritanceNode parent)
        {
            InheritanceNode node = new InheritanceNode(height, parent);
            node.setContent(content);
            node.setContextSymbol(contextSymbol);
            node.setInheritanceSymbol(inheritanceSymbol);
            node.setRawContent(rawContent);
            node.setRawContexts(rawContexts);

            ComputeEntry entry = new ComputeEntry(rawContent, rawContexts);

            if(computeWeaken)
                if(hashSet.get() == null)
                {
                    hashSet.set(new HashSet<>());
                    hashSet.get().add(entry);
                    node.setWeaken(weaken);
                }
                else
                    if(!hashSet.get().add(entry))
                        node.setWeaken(true);
                    else
                        node.setWeaken(weaken);
            else
                node.setWeaken(weaken);

            return node;
        }

        private Reference<Set<ComputeEntry>> hashSet;

        private final boolean computeWeaken;

        private Set<Context> rawContexts;

        private Text content;

        private Text contextSymbol;

        private Text inheritanceSymbol;

        private String rawContent;

        private boolean weaken;

        private final List<Builder> children = new ArrayList<>();

        private final Builder parent;

        public static final class ComputeEntry
        {
            private ComputeEntry(String rawContent, Set<Context> rawContexts)
            {
                this.rawContent = rawContent;
                this.rawContexts = rawContexts;
            }

            @Override
            public boolean equals(Object obj)
            {
                if(obj == null)
                    return false;

                if(!(obj instanceof ComputeEntry))
                    return false;

                ComputeEntry object = (ComputeEntry) obj;

                // assert rawContent != null;
                // assert rawContexts != null;

                return object.rawContent.equals(rawContent)
                        && object.rawContexts.equals(rawContexts);
            }

            @Override
            public int hashCode()
            {
                return Objects.hash(rawContent, rawContexts);
            }

            public String getRawContent()
            {
                return rawContent;
            }

            public Set<Context> getRawContexts()
            {
                return Collections.unmodifiableSet(rawContexts);
            }

            private final Set<Context> rawContexts;

            private final String rawContent;
        }
    }
}
