# ---------------------------------------------------------
# Navigation Component custom arguments
# ---------------------------------------------------------
# Fixes release crash:
# ClassNotFoundException when Navigation XML references custom argType.

-keep class com.miassolutions.milkledger.features.expense.domain.Expense { *; }
-keepnames class com.miassolutions.milkledger.features.expense.domain.Expense


# ---------------------------------------------------------
# Parcelable support
# ---------------------------------------------------------

-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}


# ---------------------------------------------------------
# Serializable support
# ---------------------------------------------------------

-keepnames class * implements java.io.Serializable

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}