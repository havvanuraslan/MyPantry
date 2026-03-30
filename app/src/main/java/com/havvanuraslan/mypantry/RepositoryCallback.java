package com.havvanuraslan.mypantry;

public interface RepositoryCallback<T> {
    void onSuccess(T data);
    void onError(String error);
}

