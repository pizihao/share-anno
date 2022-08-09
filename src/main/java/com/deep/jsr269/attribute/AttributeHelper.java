package com.deep.jsr269.attribute;

import com.sun.tools.javac.code.Attribute;

/**
 * <h2></h2>
 *
 * @author Create by liuwenhao on 2022/8/9 16:54
 */
public class AttributeHelper {

    private AttributeHelper(){}

    public static AttributeAdapt attributeAdapt(Attribute value) {
        if (value.getClass().isAssignableFrom(Attribute.Enum.class)) {
            return new EnumAttribute();
        } else if (value.getClass().isAssignableFrom(Attribute.Class.class)) {
            return new ClassAttribute();
        } else if (value.getClass().isAssignableFrom(Attribute.Compound.class)) {
            return new CompoundAttribute();
        } else if (value.getClass().isAssignableFrom(Attribute.Array.class)) {
            return new ArrayAttribute();
        } else {
            return new ConstAttribute();
        }
    }

}