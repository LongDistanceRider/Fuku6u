package fuku6u.Expectation;

public enum Parameter {
    conviction(1000),
    convictionPossessedWerewolf(500),
    veryTrust(-500),
    graveDoubts(250),
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
