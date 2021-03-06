/*
 *  Copyright Roman Donchenko. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.drextended.databinding.viewmodel;

import android.content.Context;
import android.databinding.ObservableField;
import android.os.Bundle;
import android.view.View;

import com.drextended.actionhandler.ActionHandler;
import com.drextended.actionhandler.action.CompositeAction;
import com.drextended.actionhandler.action.CompositeAction.ActionItem;
import com.drextended.actionhandler.action.DialogAction;
import com.drextended.actionhandler.listener.ActionInterceptor;
import com.drextended.actionhandler.listener.OnActionFiredListener;
import com.drextended.databinding.ActionType;
import com.drextended.databinding.R;
import com.drextended.databinding.action.OpenSecondActivity;
import com.drextended.databinding.action.SampleRequestAction;
import com.drextended.databinding.action.SampleRxRequestAction;
import com.drextended.databinding.action.ShowToastAction;
import com.drextended.databinding.action.SimpleAnimationAction;
import com.drextended.databinding.action.TrackAction;

/**
 * Created on 15.06.2016.
 */

public class MainActivityViewModel extends BaseViewModel implements OnActionFiredListener, ActionInterceptor {

    private static final String EXTRA_LAST_ACTION_TEXT = "EXTRA_LAST_ACTION_TEXT";

    public ObservableField<String> lastActionText = new ObservableField<>();
    public ObservableField<String> model = new ObservableField<>();
    public ActionHandler actionHandler;

    private int mClickCount;
    private Callback mCallback;

    public MainActivityViewModel(Context context, Callback callback) {
        super(context);
        mCallback = callback != null ? callback : Callback.EMPTY_CALLBACK;
        actionHandler = buildActionHandler();
        refreshModel();
    }

    private void refreshModel() {
        model.set("Model (" + System.currentTimeMillis() + ")");
    }

    private ActionHandler buildActionHandler() {
        return new ActionHandler.Builder()
                .addAction(null, new SimpleAnimationAction()) // Applied for any actionType
                .addAction(null, new TrackAction()) // Applied for any actionType
                .addAction(ActionType.OPEN_NEW_SCREEN, new OpenSecondActivity())
                .addAction(ActionType.FIRE_ACTION, new ShowToastAction())
                .addAction(ActionType.FIRE_DIALOG_ACTION, DialogAction.wrap(getString(R.string.action_dialog_message), new ShowToastAction()))
                .addAction(ActionType.FIRE_REQUEST_ACTION, new SampleRequestAction())
                .addAction(ActionType.FIRE_COMPOSITE_ACTION,
                        new CompositeAction<String>(new CompositeAction.TitleProvider<String>() {
                            @Override
                            public String getTitle(Context context, String model) {
                                return "Title (" + model + ")";
                            }
                        },
                                new ActionItem(ActionType.OPEN_NEW_SCREEN, new OpenSecondActivity(), R.string.fire_intent_action),
                                new ActionItem(ActionType.FIRE_ACTION, new ShowToastAction(), R.string.fire_simple_action),
                                new ActionItem(ActionType.FIRE_DIALOG_ACTION, DialogAction.wrap(getString(R.string.action_dialog_message), new ShowToastAction()), R.string.fire_dialog_action),
                                new ActionItem(ActionType.FIRE_REQUEST_ACTION, new SampleRequestAction(), R.string.fire_request_action),
                                new ActionItem(ActionType.FIRE_RX_REQUEST_ACTION, new SampleRxRequestAction(), R.string.fire_rx_request_action)
                        ))
                .setActionInterceptor(this)
                .setActionFiredListener(this)
                .build();
    }

    @Override
    public boolean onInterceptAction(Context context, View view, String actionType, Object model) {
        switch (actionType) {
            case ActionType.OPEN_NEW_SCREEN:
                final boolean consumed = mClickCount++ % 7 == 0;
                if (consumed) {
                    mCallback.showMessage(getString(R.string.message_action_intercepted));
                }
                return consumed;
//            case ActionType.FIRE_ACTION:
//            case ActionType.FIRE_DIALOG_ACTION:
//            case ActionType.FIRE_REQUEST_ACTION:
        }
        return false;
    }

    @Override
    public void onActionFired(View view, String actionType, Object model) {
        switch (actionType) {
            case ActionType.OPEN_NEW_SCREEN:
                lastActionText.set("Intent Action");
                break;
            case ActionType.FIRE_ACTION:
                lastActionText.set("Simple Action");
                break;
            case ActionType.FIRE_DIALOG_ACTION:
                lastActionText.set("Dialog Action");
                break;
            case ActionType.FIRE_REQUEST_ACTION:
                lastActionText.set("Request Action");
                break;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putString(EXTRA_LAST_ACTION_TEXT, lastActionText.get());
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        lastActionText.set(savedInstanceState.getString(EXTRA_LAST_ACTION_TEXT));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        actionHandler.cancelAll();
    }

    public interface Callback {

        void showMessage(String message);

        Callback EMPTY_CALLBACK = new Callback() {
            @Override
            public void showMessage(String message) {
            }
        };
    }
}
