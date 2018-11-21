package com.example.mauro.glovoclient.presenters

/**
 * Name: BasePresenter
 *
 * Purpose: Base class for presenters model
 */
abstract class BasePresenter<V> {

    protected var view: V? = null

    fun attachView(view: V) {
        this.view = view
    }

    fun detachView() {
        this.view = null
    }
}