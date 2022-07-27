/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.wm.shell.splitscreen;

import android.annotation.Nullable;
import android.app.ActivityManager;
import android.content.Context;
import android.view.SurfaceSession;
import android.window.WindowContainerToken;
import android.window.WindowContainerTransaction;

import com.android.launcher3.icons.IconProvider;
import com.android.wm.shell.ShellTaskOrganizer;
import com.android.wm.shell.common.SyncTransactionQueue;

/**
 * Side stage for split-screen mode. Only tasks that are explicitly pinned to this stage show up
 * here. All other task are launch in the {@link MainStage}.
 *
 * @see StageCoordinator
 */
class SideStage extends StageTaskListener {
    private static final String TAG = SideStage.class.getSimpleName();

    SideStage(Context context, ShellTaskOrganizer taskOrganizer, int displayId,
            StageListenerCallbacks callbacks, SyncTransactionQueue syncQueue,
            SurfaceSession surfaceSession, IconProvider iconProvider,
            @Nullable StageTaskUnfoldController stageTaskUnfoldController) {
        super(context, taskOrganizer, displayId, callbacks, syncQueue, surfaceSession, iconProvider,
                stageTaskUnfoldController);
    }

    boolean removeAllTasks(WindowContainerTransaction wct, boolean toTop) {
        // No matter if the root task is empty or not, moving the root to bottom because it no
        // longer preserves visible child task.
        wct.reorder(mRootTaskInfo.token, false /* onTop */);
        if (mChildrenTaskInfo.size() == 0) return false;
        wct.reparentTasks(
                mRootTaskInfo.token,
                null /* newParent */,
                CONTROLLED_WINDOWING_MODES_WHEN_ACTIVE,
                CONTROLLED_ACTIVITY_TYPES,
                toTop);
        return true;
    }

    boolean removeTask(int taskId, WindowContainerToken newParent, WindowContainerTransaction wct) {
        final ActivityManager.RunningTaskInfo task = mChildrenTaskInfo.get(taskId);
        if (task == null) return false;
        wct.reparent(task.token, newParent, false /* onTop */);
        return true;
    }
}