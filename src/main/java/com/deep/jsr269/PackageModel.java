package com.deep.jsr269;

import java.util.Objects;

/**
 * 需要导入的包
 *
 * @author Create by liuwenhao on 2022/8/2 14:17
 */
public class PackageModel {

    String packageName;

    String className;

    public PackageModel(String packageName, String className) {
        this.packageName = packageName;
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackageModel that = (PackageModel) o;
        return Objects.equals(packageName, that.packageName) && Objects.equals(className, that.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName, className);
    }

    @Override
    public String toString() {
        return "PackageModel{" +
            "packageName='" + packageName + '\'' +
            ", className='" + className + '\'' +
            '}';
    }
}