package com.nova.app.widget
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.nova.app.R

class NovaWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_nova)
            views.setTextViewText(R.id.widget_status, "Listening")
            manager.updateAppWidget(id, views)
        }
    }
    companion object {
        fun setLastCommand(context: Context, cmd: String) {}
    }
}