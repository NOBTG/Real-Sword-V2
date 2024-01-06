package com.nobtgRealSword.utils;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import static it.unimi.dsi.fastutil.HashCommon.maxFill;

public final class Int2ObjectMapUtil<V> {
    private static final List<String> blackField = List.of("values", "keys", "entries", "map");
    private transient int[] key;
    private transient V[] value;
    private transient int mask;
    private transient boolean containsNullKey;
    private transient int first = -1;
    private transient int last = -1;
    private transient long[] link;
    private transient int n;
    private transient int maxFill;
    private transient int minN;
    private int size;
    private float f;
    private final Int2ObjectLinkedOpenHashMap<V> map;

    private Int2ObjectMapUtil(Int2ObjectLinkedOpenHashMap<V> map) {
        this.map = map;
        try {
            Field[] fields = Int2ObjectMapUtil.class.getDeclaredFields();
            AccessibleObject.setAccessible(fields, true);

            for (Field field : fields) {
                if (!Modifier.isStatic(field.getModifiers()) && !blackField.contains(field.getName())) {
                    Field field1 = Int2ObjectLinkedOpenHashMap.class.getDeclaredField(field.getName());
                    field1.setAccessible(true);
                    field.set(this, field1.get(this.map));
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static <K> Int2ObjectMapUtil<K> getInstance(Int2ObjectLinkedOpenHashMap<K> map) {
        return new Int2ObjectMapUtil<>(map);
    }

    public Int2ObjectMapUtil<V> remove(final int k) {
        if (((k) == (0))) {
            if (containsNullKey) return removeNullEntry();
            return this;
        }
        int curr;
        final int[] key = this.key;
        int pos;
        if (((curr = key[pos = (HashCommon.mix((k))) & mask]) == (0))) return this;
        if (((k) == (curr))) return removeEntry(pos);
        while (true) {
            if (((curr = key[pos = (pos + 1) & mask]) == (0))) return this;
            if (((k) == (curr))) return removeEntry(pos);
        }
    }

    public Int2ObjectLinkedOpenHashMap<V> synchronize() {
        try {
            Field[] fields = Int2ObjectLinkedOpenHashMap.class.getDeclaredFields();
            AccessibleObject.setAccessible(fields, true);

            for (Field field : fields) {
                if (!Modifier.isStatic(field.getModifiers()) && !blackField.contains(field.getName())) {
                    Field field1 = Int2ObjectMapUtil.class.getDeclaredField(field.getName());
                    field1.setAccessible(true);
                    field.set(this.map, field1.get(this));
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        return this.map;
    }

    private Int2ObjectMapUtil<V> removeNullEntry() {
        containsNullKey = false;
        value[n] = null;
        size--;
        fixPointers(n);
        if (n > minN && size < maxFill / 4 && n > Hash.DEFAULT_INITIAL_SIZE) rehash(n / 2);
        return this;
    }

    @SuppressWarnings("unchecked")
    private void rehash(final int newN) {
        final int[] key = this.key;
        final V[] value = this.value;
        final int mask = newN - 1;
        final int[] newKey = new int[newN + 1];
        final V[] newValue = (V[]) new Object[newN + 1];
        int i = first, prev = -1, newPrev = -1, t, pos;
        final long[] link = this.link;
        final long[] newLink = new long[newN + 1];
        first = -1;
        for (int j = size; j-- != 0; ) {
            if (((key[i]) == (0))) pos = newN;
            else {
                pos = (HashCommon.mix((key[i]))) & mask;
                while (!((newKey[pos]) == (0))) pos = (pos + 1) & mask;
            }
            newKey[pos] = key[i];
            newValue[pos] = value[i];
            if (prev != -1) {
                newLink[newPrev] ^= ((newLink[newPrev] ^ (pos & 0xFFFFFFFFL)) & 0xFFFFFFFFL);
                newLink[pos] ^= ((newLink[pos] ^ ((newPrev & 0xFFFFFFFFL) << 32)) & 0xFFFFFFFF00000000L);
                newPrev = pos;
            } else {
                newPrev = first = pos;
                newLink[pos] = -1L;
            }
            t = i;
            i = (int) link[i];
            prev = t;
        }
        this.link = newLink;
        this.last = newPrev;
        if (newPrev != -1) newLink[newPrev] |= 0xFFFFFFFFL;
        n = newN;
        this.mask = mask;
        maxFill = maxFill(n, f);
        this.key = newKey;
        this.value = newValue;
    }

    private void fixPointers(final int i) {
        if (size == 0) {
            first = last = -1;
            return;
        }
        if (first == i) {
            first = (int) link[i];
            if (0 <= first) {
                link[first] |= (0xFFFFFFFFL) << 32;
            }
            return;
        }
        if (last == i) {
            last = (int) (link[i] >>> 32);
            if (0 <= last) {
                link[last] |= 0xFFFFFFFFL;
            }
            return;
        }
        final long linki = link[i];
        final int prev = (int) (linki >>> 32);
        final int next = (int) linki;
        link[prev] ^= ((link[prev] ^ (linki & 0xFFFFFFFFL)) & 0xFFFFFFFFL);
        link[next] ^= ((link[next] ^ (linki & 0xFFFFFFFF00000000L)) & 0xFFFFFFFF00000000L);
    }

    private Int2ObjectMapUtil<V> removeEntry(final int pos) {
        value[pos] = null;
        size--;
        fixPointers(pos);
        shiftKeys(pos);
        if (n > minN && size < maxFill / 4 && n > Hash.DEFAULT_INITIAL_SIZE) rehash(n / 2);
        return this;
    }

    private void shiftKeys(int pos) {
        int last, slot;
        int curr;
        final int[] key = this.key;
        for (; ; ) {
            pos = ((last = pos) + 1) & mask;
            for (; ; ) {
                if (((curr = key[pos]) == (0))) {
                    key[last] = (0);
                    value[last] = null;
                    return;
                }
                slot = (HashCommon.mix((curr))) & mask;
                if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) break;
                pos = (pos + 1) & mask;
            }
            key[last] = curr;
            value[last] = value[pos];
            fixPointers(pos, last);
        }
    }

    private void fixPointers(int s, int d) {
        if (size == 1) {
            first = last = d;
            link[d] = -1L;
            return;
        }
        if (first == s) {
            first = d;
            link[(int) link[s]] ^= ((link[(int) link[s]] ^ ((d & 0xFFFFFFFFL) << 32)) & 0xFFFFFFFF00000000L);
            link[d] = link[s];
            return;
        }
        if (last == s) {
            last = d;
            link[(int) (link[s] >>> 32)] ^= ((link[(int) (link[s] >>> 32)] ^ (d & 0xFFFFFFFFL)) & 0xFFFFFFFFL);
            link[d] = link[s];
            return;
        }
        final long links = link[s];
        final int prev = (int) (links >>> 32);
        final int next = (int) links;
        link[prev] ^= ((link[prev] ^ (d & 0xFFFFFFFFL)) & 0xFFFFFFFFL);
        link[next] ^= ((link[next] ^ ((d & 0xFFFFFFFFL) << 32)) & 0xFFFFFFFF00000000L);
        link[d] = links;
    }
}
