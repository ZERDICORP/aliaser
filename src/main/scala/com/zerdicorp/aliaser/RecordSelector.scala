package com.zerdicorp.aliaser

final case class RecordSelector(
    private var _first: AliasRecordId = -1,
    private var _second: AliasRecordId = -1,
) {
  def first: AliasRecordId = _first
  def second: AliasRecordId = _second
  def reset(): Unit = {
    _first = -1
    _second = -1
  }
  def select(recordId: AliasRecordId)(
      select: Unit => Unit,
      unselect: Unit => Unit,
      finalize: Unit => Unit,
  ): Unit = {
    if (_first == recordId || _second == recordId) {
      if (_first == recordId) _first = -1
      else if (_second == recordId) _second = -1
      unselect()
    } else {
      if (_first == -1) {
        _first = recordId
        select()
      } else {
        if (_second == -1) {
          _second = recordId
          select()
        }
      }
      if (_first != -1 && _second != -1) {
        finalize()
      }
    }
  }
}
