package com.daon.gitrepo_part4_05.extensions

import android.content.res.Resources

internal fun Float.fromDpToPx() =
    (this * Resources.getSystem().displayMetrics.density).toInt()