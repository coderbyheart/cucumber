package io.cucumber.cucumberexpressions;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class TransformLookup {
    private static final List<String> FIXNUM_REGEXPS = asList("-?\\d+", "\\d+");
    private static final List<String> FLOATING_POINT_REGEXPS = singletonList("-?\\d*[\\.,]\\d+");

    private final Map<Type, Transform<?>> transformsByType = new HashMap<>();
    private final Map<String, Transform<?>> transformsByTypeName = new HashMap<>();
    private final Map<String, Transform<?>> transformsByCaptureGroupRegexp = new HashMap<>();

    public TransformLookup(Locale locale) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        final NumberParser numberParser = new NumberParser(numberFormat);

        addTransform(new SimpleTransform<>("byte", byte.class, FIXNUM_REGEXPS, new Function<String, Byte>() {
            @Override
            public Byte apply(String s) {
                return numberParser.parseByte(s);
            }
        }));
        addTransform(new SimpleTransform<>("byte", Byte.class, FIXNUM_REGEXPS, new Function<String, Byte>() {
            @Override
            public Byte apply(String s) {
                return numberParser.parseByte(s);
            }
        }));
        addTransform(new SimpleTransform<>("short", short.class, FIXNUM_REGEXPS, new Function<String, Short>() {
            @Override
            public Short apply(String s) {
                return numberParser.parseShort(s);
            }
        }));
        addTransform(new SimpleTransform<>("short", Short.class, FIXNUM_REGEXPS, new Function<String, Short>() {
            @Override
            public Short apply(String s) {
                return numberParser.parseShort(s);
            }
        }));
        addTransform(new SimpleTransform<>("int", int.class, FIXNUM_REGEXPS, new Function<String, Integer>() {
            @Override
            public Integer apply(String s) {
                return numberParser.parseInt(s);
            }
        }));
        addTransform(new SimpleTransform<>("int", Integer.class, FIXNUM_REGEXPS, new Function<String, Integer>() {
            @Override
            public Integer apply(String s) {
                return numberParser.parseInt(s);
            }
        }));
        addTransform(new SimpleTransform<>("long", long.class, FIXNUM_REGEXPS, new Function<String, Long>() {
            @Override
            public Long apply(String s) {
                return numberParser.parseLong(s);
            }
        }));
        addTransform(new SimpleTransform<>("long", Long.class, FIXNUM_REGEXPS, new Function<String, Long>() {
            @Override
            public Long apply(String s) {
                return numberParser.parseLong(s);
            }
        }));
        addTransform(new SimpleTransform<>("float", float.class, FLOATING_POINT_REGEXPS, new Function<String, Float>() {
            @Override
            public Float apply(String s) {
                return numberParser.parseFloat(s);
            }
        }));
        addTransform(new SimpleTransform<>("float", Float.class, FLOATING_POINT_REGEXPS, new Function<String, Float>() {
            @Override
            public Float apply(String s) {
                return numberParser.parseFloat(s);
            }
        }));
        addTransform(new SimpleTransform<>("double", double.class, FLOATING_POINT_REGEXPS, new Function<String, Double>() {
            @Override
            public Double apply(String s) {
                return numberParser.parseDouble(s);
            }
        }));
        addTransform(new SimpleTransform<>("double", Double.class, FLOATING_POINT_REGEXPS, new Function<String, Double>() {
            @Override
            public Double apply(String s) {
                return numberParser.parseDouble(s);
            }
        }));
    }

    public void addTransform(Transform<?> transform) {
        transformsByType.put(transform.getType(), transform);
        transformsByTypeName.put(transform.getTypeName(), transform);

        for (String captureGroupRegexp : transform.getCaptureGroupRegexps()) {
            transformsByCaptureGroupRegexp.put(captureGroupRegexp, transform);
        }
    }

    private <T> Transform<T> lookupByType(Type type) {
        return (Transform<T>) transformsByType.get(type);
    }

    private Transform<?> lookupByTypeName(String typeName, boolean ignoreUnknownTypeName) {
        Transform<?> transform = transformsByTypeName.get(typeName);
        if (transform == null) {
            if (ignoreUnknownTypeName) {
                return null;
            } else {
                throw new CucumberExpressionException(String.format("No transform for type name \"%s\"", typeName));
            }
        }
        return transform;
    }

    private Transform lookupByCaptureGroupRegexp(String captureGroupPattern) {
        return transformsByCaptureGroupRegexp.get(captureGroupPattern);
    }

    Collection<Transform<?>> getTransforms() {
        return transformsByType.values();
    }

    public Transform<?> lookupTransform(Type type, String captureGroupPattern) {
        Transform<?> transform = null;
        if (type != null) {
            transform = lookupByType(type);
        }
        if (transform == null) {
            transform = lookupByCaptureGroupRegexp(captureGroupPattern);
        }
        if (transform == null && type != null && type instanceof Class) {
            transform = new ClassTransform<>((Class) type);
        }
        if (transform == null) {
            transform = new ConstructorTransform<>(String.class);
        }
        return transform;
    }

    public Transform<?> lookupTransform(Type type, String parameterName, String typeName) {
        Transform<?> transform = null;
        if (type != null) {
            transform = lookupByType(type);
        }
        if (transform == null && typeName != null) {
            transform = lookupByTypeName(typeName, false);
        }
        if (transform == null) {
            transform = lookupByTypeName(parameterName, true);
        }
        if (transform == null && type != null && type instanceof Class) {
            transform = new ClassTransform<>((Class) type);
        }
        if (transform == null) {
            transform = new ConstructorTransform<>(String.class);
        }
        return transform;
    }
}
