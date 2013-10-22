BS.Flaky = {
  start: function(projectId) {
    var progressIcon = $j("#startAnalysisProgress");
    progressIcon.show();

    var parameters = {
      projectId: projectId,
      excludeBuildTypes: ($j("#excludeBuildTypes").val() || []).join(":"),
      analyseTimePeriodDays: $j("#analyseTimePeriodDays").val(),
      analyseFullHistory: $j("#analyseFullHistory").is(":checked"),
      speedUpAlwaysFailing: $j("#speedUpAlwaysFailing").is(":checked"),
      minSeriesNumber: $j("#minSeriesNumber").val(),
      averageSeriesLength: $j("#averageSeriesLength").val()
    };

    BS.ajaxRequest("analyzeTests.html", {
      method: "POST",
      parameters: parameters,
      onComplete: function() {
        progressIcon.hide();
        $("flaky").refresh();
      }
    });
    return false;
  },

  scheduleReload: function() {
    setTimeout(function() {
      $("flaky").refresh();
    }, 2000);
  },

  scrollAndExpandIfNeeded: function() {
    var hash = window.location.hash;
    if (hash) {
      var testId = hash.substring(5);                         // cut off the "#test" prefix
      var spanId = "testNameId" + testId;
      var link = $j("#" + spanId + " a");
      $j("body").animate({scrollTop: link.position().top});   // scroll
      link.trigger("click");                                  // expand
    }
  }
};

BS.Flaky.Dialog = OO.extend(BS.AbstractModalDialog, {
  show: function() {
    this.showCentered();
    this.initTabs();
    this.initFields();
    return false;
  },

  initTabs: function() {
    $j("#dialog-tabs a").each(function() {
      var link = $j(this);
      var li = link.parent().parent();
      var id = li.attr("id");
      link.click(function() {
        $j("#dialog-contents > div").hide();
        $j("#" + id + "-content").show();
        li.addClass("selected")
          .siblings().removeClass("selected");
        return false;
      });
    })
  },

  initFields: function() {
    this._saveValues();
    $j("#analyseFullHistory").change(function() {
      var checked = $j(this).is(":checked");
      $j("#analyseTimePeriodDays").prop("disabled", checked);
    });
  },

  validate: function() {
    var period = $j("#analyseTimePeriodDays").val();
    if (!this._isInteger(period)) {
      alert("Incorrect time period value");
      return false;
    }
    if (!this._isPositive(period)) {
      alert("Incorrect time period value: should be positive");
      return false;
    }

    var seriesNum = $j("#minSeriesNumber").val();
    if (!this._isInteger(seriesNum)) {
      alert("Incorrect series number value");
      return false;
    }
    if (!this._isPositive(seriesNum)) {
      alert("Incorrect series number value: should be positive");
      return false;
    }

    var seriesLength = $j("#averageSeriesLength").val();
    if (!this._isFloat(seriesLength)) {
      alert("Incorrect series length value");
      return false;
    }
    if (!this._isPositive(seriesLength)) {
      alert("Incorrect series length value: should be positive");
      return false;
    }

    return true;
  },

  cancel: function() {
    this._restoreValues();
    this.doClose();
    return false;
  },

  start: function(projectId) {
    if (this.validate()) {
      BS.Flaky.start(projectId);
      this.doClose();
    }
    return false;
  },

  // http://stackoverflow.com/questions/1019515/javascript-test-for-an-integer
  _isInteger: function(value) {
    return (parseFloat(value) == parseInt(value)) && !isNaN(value);
  },

  _isPositive: function(value) {
    return parseFloat(value) > 0;
  },

  _isFloat: function(value) {
    return !isNaN(parseFloat(value));
  },

  _saveValues: function() {
    if (this._values) return;

    var values = this._values = {};
    $j("#dialog-contents").find("input[type=text], select").each(function() {
      var self = $j(this);
      values[self.attr("id")] = self.val();
    });
    $j("#dialog-contents").find("input[type=checkbox]").each(function() {
      var self = $j(this);
      values[self.attr("id")] = self.prop("checked");
    });
  },

  _restoreValues: function() {
    var values = this._values;
    $j("#dialog-contents").find("input[type=text], select").each(function() {
      var self = $j(this);
      self.val(values[self.attr("id")]).trigger("change");
    });
    $j("#dialog-contents").find("input[type=checkbox]").each(function() {
      var self = $j(this);
      self.prop("checked", values[self.attr("id")]).trigger("change");
    });
  },

  getContainer: function() {
    return $('settingsFormDialog');
  },

  formElement: function() {
    return $('settingsForm');
  }
});

BS.TestDetails._toggleDetails = BS.TestDetails.toggleDetails;
BS.TestDetails.toggleDetails = function(link, url) {
  var idx = url.indexOf("?");
  url = "/flakyTestDetails.html" + url.substr(idx);

  // Should be compatible with 7.1.x and 8.0.
  idx = url.indexOf("projectId=");
  url = url.substr(0, idx) + "projectId=" + BS.projectId;

  return this._toggleDetails(link, url);
};
