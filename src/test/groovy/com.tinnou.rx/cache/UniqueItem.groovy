package com.tinnou.rx.cache


class UniqueItem {
    final def position

    UniqueItem(long pos) {
        position = "Position:" + pos
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UniqueItem{");
        sb.append("position=").append(position);
        sb.append('}');
        return sb.toString();
    }
}