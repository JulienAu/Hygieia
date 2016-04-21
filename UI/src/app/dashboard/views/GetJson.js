function Conf(x , y)
{
  if(parseInt(y) < 0){
    if(parseInt(x) < Math.abs(parseInt(y))){
        return "<div id=\"moncercle\"></div>";
    } 
    return "";
  } else {
 if(parseInt(x) > parseInt(y)){
  return "<div id=\"moncercle\"></div>";
  }
  return "";
  }
}


function getjsonConf(url , callback){
  var array= new Array(101);
  for(i =0 ; i < 100 ; i++){
    array[i]=999999;
  }
  $(document).ready(function() {
                $.getJSON(url, function(jd) {
                  var k= 1;
                $.each(jd, function(i, field){
                  if ( i == "Conf") {
                    if(jd.Conf.length > 0){
                        $.each(jd.Conf[0], function(i, field){
                          if(k == 4 ){
                            array[k] = 999999;
                            array[k+1] = 999999;
                            array[k+2] = field;
                            k = k + 2;
                          } else {
                            array[k] = field;
                          }
                          k++;
                          });
                      }
                      callback(array);
                    }
                  });
              });
   });
}


function getjsonCodeCoverage(url ,array , callback){
  $(document).ready(function() {
                $.getJSON(url, function(jd) {
                  var k= 0;
                $.each(jd, function(i, field){
                  if ( i == "result") {
                    if(jd.result.length > 0){
                        $.each(jd.result[jd.result.length-1], function(i, field){
                            switch(i) {
                            case "lineCoverage":
                                  array[9]=field;
                              break;
                            case "functionCoverage":
                                 array[10]=field;
                            break;
                            case "branchCoverage":
                                  array[11]=field;
                            break;
                            case "lineCoverageUnitaire":
                                  array[12]=field;
                              break;
                            case "functionCoverageUnitaire":
                                  array[13]=field;
                            break;
                            case "branchCoverageUnitaire":
                                  array[14]=field;
                            break;
                            case "bytesLostValgrind":
                                  array[15]=field;
                            break;
                            case "duplicateCodeWarnings":
                                  array[16]=field;
                            break;
                            case "duplicateCodeHigh":
                                  array[17]=field;
                            break;
                            case "duplicateCodeMedium":
                                  array[18]=field;
                            break;   
                             case "duplicateCodeLow":
                                  array[19]=field;
                            break;                  
                            default:
                              k = 100;
                        } 
                          });
                      }
                      callback(array);
                    }
                  });
              });
   });
}


function getjsonJenkins(url ,array, callback){
  $(document).ready(function() {
                $.getJSON(url, function(jd) {
                  var k= 0;
                $.each(jd, function(i, field){
                  if ( i == "result"  ) {
                    if(jd.result.length > 0){
                        $.each(jd.result[jd.result.length-1], function(i, field){
                            switch(i) {
                            case "buildStatus":
                                  array[5]=field;
                              break;
                            case "failCount":
                              if(field != "none"){
                                 array[6]=field;
                              }
                            break;
                            case "skipCount":
                              if(field != "none"){
                                 array[7]=field;
                              }
                            break;
                            case "passCount":
                              if(field != "none"){
                                  array[8]=field;
                                }
                            break;
                            case "numberWarningSeverityCpp":
                                  array[20]=field;
                            break;
                             case "numberErrorSeverityCpp":
                                 array[21]=field;
                            break;                           
                              default:
                              k = 100;
                            } 
                          });
                        }
                        callback(array);
                        }
                    });
              });
   });
}



function getProject(url) {
  $(document).ready(function() {
                var i=0;
                var cookieValue = document.cookie.replace(/(?:(?:^|.*;\s*)username\s*\=\s*([^;]*).*$)|^.*$/, "$1");                    
                var url2 = url+cookieValue;
                var id="";
                var idDashboard="";
                var title ="";
                $.getJSON(url2, function(jd) {
                  $.each(jd, function(i, field){
                    idDashboard = field.id;
                    title=field.title;
                    $.each(field, function(i, field){
                      if (i == "widgets"){
                        $.each(field, function(i, field){
                          if(field.componentId != id) {
                            id = field.componentId;
                            i += 1;
                            getjsonComparaison(id , idDashboard , title);
                            }    
                          });
                        }
                      });
                  });
              });
  });

}


function getjsonComparaison(id , idDashboard , title) {
  url = 'api/quality/static-analysis?componentId='+id+'&max=5';
  var array= new Array(101);
  var array2 = new Array(101);
  for (i = 0; i < 100; i++) {
    array[i] = " - ";
  }
  $(document).ready(function() {
                $.getJSON(url, function(jd) {
                var txt = "<tr><td><a href=\"#/dashboard/"+idDashboard+"\">"+title+" </a></td>";
                var k = 0;
                $.each(jd, function(i, field){                  
                    if ( i == "result" ) {
                      for (var j = 0 ; j< field.length ; j++){
                        $.each(jd.result[j], function(i, field){
                          if ( i == "metrics" ) {
                          for (i = 0; i < field.length; i++) { 
                            switch(jd.result[j].metrics[i].name) {
                            case "line_coverage":
                                  k = 3;
                              break;
                            case "blocker_violations":
                                  k = 1;
                            break;
                            case "tests":
                                  k = 4;
                            break;
                            case "major_violations":
                                  k = 2;
                            break;
                             case "ncloc":
                                  k = 0;
                            break;
                            case "violations":
                                  k = 32;
                            break;
                            case "critical_violations":
                                  k = 31;
                            break;
                              default:
                              k = 100; //
                        } 
                        $.each(jd.result[j].metrics[i], function(i, field){
                          if(i == "value"){
                            if(array[k] >= 0){
                              if(field != array[k]){
                                if(!(array2[k] >= 0)){
                                  array2[k]=field;
                                }
                              }
                            }else{
                              array[k]=field;
                            }
                          }
                        });
                      }
                    
                    }
                  });
                }
                }
                });
                    getjsonJenkins('api/build/?componentId='+id+'&max=1' ,array,function(array) {
                      getjsonCodeCoverage('api/build2/?componentId='+id+'&max=1' ,array,function(array) {
                        getjsonConf('Conf.json' ,function(array3) {
                         array[1] += array[31];
                         array[2] += array[32];
                         if(array[3] == 0){
                          array[3] = array[12];
                         }
                         for (i = 31; i < 33; i++) {
                            if(!(array2[i]=== undefined)){
                                array2[i-30] += array2[i];
                            }
                          }
                          k=-1;
                          var status = 0;
                          for (i = 0; i < 22; i++) {
                            k++;
                            var color = Conf(array[i] , array3[k]);
                            if(color.length > 0){
                              status=1;
                            }
                            txt += "<td>"+ color;
                            if(array2[i]=== undefined){
                             if(i == 6){
                                  txt += "F : "+array[i]+"<br /> S : "+array[i+1]+"<br /> P : "+array[i+2]+" </td>";
                                  i = i+2;
                              }
                              else if(i == 9 || i == 12){
                                txt += "L : "+array[i]+"<br /> F : "+array[i+1]+"<br /> B : "+array[i+2]+" </td>";
                                i = i+2;
                              }
                              else if(i == 16){
                                txt += "W : "+array[i]+"<br /> H : "+array[i+1]+"</font><br /> M : "+array[i+2]+"<br /> L : "+array[i+3]+" </td>";
                                i = i+3;
                              }
                              else{
                              txt += array[i]+"</td>";
                            }
                           } else {
                              if ((array[i] - array2[i]) > 0){
                                txt += array[i]+" <br /> (↑+"+ Math.round((array[i] - array2[i])*100)/100+ ")</td>";
                              }else{
                              txt += array[i]+" <br /> (↓"+ Math.round((array[i] - array2[i])*100)/100+ ")</td>";
                            }
                          }
                        }
                        if(status == 1){
                          txt += "<td><img src=\"status/nuage.png\"></td>";
                        }else{
                          txt += "<td><img src=\"status/soleil.png\"></td>";
                        }
                       if(txt != ""){
                        txt +="<td><a href ng-click=\"ctrl.deleteDashboard("+idDashboard+")\"><span class=\"fa fa-trash-o fa-lg text-danger\"></span></a></td></tr>";
                        $("#table1").append(txt).removeClass("hidden");
                        }
                    });
                });
              });
            });
          });

}

