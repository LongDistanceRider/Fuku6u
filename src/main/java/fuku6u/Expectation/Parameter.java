package fuku6u.Expectation;

public enum Parameter {
    conviction(1000),
    convictionPossessedWerewolf(500),
    unlikely(-100),
    likely(100);


    private int value;

    Parameter(int value) {
        this.value = value;
    }

    public int getInt() {
        return this.value;
    }



}
