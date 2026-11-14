package com.fpvdrone.sync;

/** How a client moves an aircraft: smoothed from server snapshots, or flown here by the local pilot. */
public enum SmoothingMode {
   REMOTE,
   LOCAL_PILOT,
   UNUSED;
}
