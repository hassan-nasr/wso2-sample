(function(){
  var Pos = CodeMirror.Pos;

  function SearchCursor(cm, query, pos, caseFold) {
    this.atOccurrence = false; this.cm = cm;
    if (caseFold == null && typeof query == "string") caseFold = false;

    pos = pos ? cm.clipPos(pos) : Pos(0, 0);
    this.pos = {from: pos, to: pos};

    // The matches method is filled in based on the type of query.
    // It takes a position and a direction, and returns an object
    // describing the next occurrence of the query, or null if no
    // more matches were found.
    if (typeof query != "string") { // Regexp match
      if (!query.global) query = new RegExp(query.source, query.ignoreCase ? "ig" : "g");
      this.matches = function(reverse, pos) {
        if (reverse) {
          query.lastIndex = 0;
          var line = cm.getLine(pos.line).slice(0, pos.ch), cutOff = 0, match, start;
          for (;;) {
            query.lastIndex = cutOff;
            var newMatch = query.exec(line);
            if (!newMatch) break;
            match = newMatch;
            start = match.index;
            cutOff = match.index + 1;
          }
        } else {
          query.lastIndex = pos.ch;
          var line = cm.getLine(pos.line), match = query.exec(line),
          start = match && match.index;
        }
        if (match && match[0])
          return {from: Pos(pos.line, start),
                  to: Pos(pos.line, start + match[0].length),
                  match: match};
      };
    } else { // String query
      if (caseFold) query = query.toLowerCase();
      var fold = caseFold ? function(str){return str.toLowerCase();} : function(str){return str;};
      var target = query.split("\n");
      // Different methods for single-line and multi-line queries
      if (target.length == 1) {
        if (!query.length) {
          // Empty string would match anything and never progress, so
          // we define it to match nothing instead.
          this.matches = function() {};
        } else {
          this.matches = function(reverse, pos) {
            var line = fold(cm.getLine(pos.line)), len = query.length, match;
            if (reverse ? (pos.ch >= len && (match = line.lastIndexOf(query, pos.ch - len)) != -1)
                        : (match = line.indexOf(query, pos.ch)) != -1)
              return {from: Pos(pos.line, match),
                      to: Pos(pos.line, match + len)};
          };
        }
      } else {
        this.matches = function(reverse, pos) {
          var ln = pos.line, idx = (reverse ? target.length - 1 : 0), match = target[idx], line = fold(cm.getLine(ln));
          var offsetA = (reverse ? line.indexOf(match) + match.length : line.lastIndexOf(match));
          if (reverse ? offsetA >= pos.ch || offsetA != match.length
              : offsetA <= pos.ch || offsetA != line.length - match.length)
            return;
          for (;;) {
            if (reverse ? !ln : ln == cm.lineCount() - 1) return;
            line = fold(cm.getLine(ln += reverse ? -1 : 1));
            match = target[reverse ? --idx : ++idx];
            if (idx > 0 && idx < target.length - 1) {
              if (line != match) return;
              else continue;
            }
            var offsetB = (reverse ? line.lastIndexOf(match) : line.indexOf(match) + match.length);
            if (reverse ? offsetB != line.length - match.length : offsetB != match.length)
              return;
            var start = Pos(pos.line, offsetA), end = Pos(ln, offsetB);
            return {from: reverse ? end : start, to: reverse ? start : end};
          }
        };
      }
    }
  }

  SearchCursor.prototype = {
    findNext: function() {return this.find(false);},
    findPrevious: function() {return this.find(true);},

    find: function(reverse) {
      var self = this, pos = this.cm.clipPos(reverse ? this.pos.from : this.pos.to);
      function savePosAndFail(line) {
        var pos = Pos(line, 0);
        self.pos = {from: pos, to: pos};
        self.atOccurrence = false;
        return false;
      }

      for (;;) {
        if (this.pos = this.matches(reverse, pos)) {
          if (!this.pos.from || !this.pos.to) { console.log(this.matches, this.pos); }
          this.atOccurrence = true;
          return this.pos.match || true;
        }
        if (reverse) {
          if (!pos.line) return savePosAndFail(0);
          pos = Pos(pos.line-1, this.cm.getLine(pos.line-1).length);
        }
        else {
          var maxLine = this.cm.lineCount();
          if (pos.line == maxLine - 1) return savePosAndFail(maxLine);
          pos = Pos(pos.line + 1, 0);
        }
      }
    },

    from: function() {if (this.atOccurrence) return this.pos.from;},
    to: function() {if (this.atOccurrence) return this.pos.to;},

    replace: function(newText) {
      if (!this.atOccurrence) return;
      var lines = CodeMirror.splitLines(newText);
      this.cm.replaceRange(lines, this.pos.from, this.pos.to);
      this.pos.to = Pos(this.pos.from.line + lines.length - 1,
                        lines[lines.length - 1].length + (lines.length == 1 ? this.pos.from.ch : 0));
    }
  };

  CodeMirror.defineExtension("getSearchCursor", function(query, pos, caseFold) {
    return new SearchCursor(this, query, pos, caseFold);
  });
})();
