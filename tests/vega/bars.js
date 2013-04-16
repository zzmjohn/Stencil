{"axes":
 [{"scale":"xscale", "type":"x"}, {"scale":"yscale", "type":"y"}],
 "data":
 [{"name":"rawData",
   "values":
   [{"x":"A", "y":28}, {"x":"B", "y":55}, {"x":"C", "y":43},
    {"x":"D", "y":91}, {"x":"E", "y":81}, {"x":"F", "y":53},
    {"x":"G", "y":19}, {"x":"H", "y":79}, {"x":"I", "y":52}]}],
 "height":200,
 "marks":
 [{"from":{"data":"rawData"},
   "properties":
   {"enter":
    {"fill":{"value":"SteelBlue"},
     "width":{"band":true, "offset":-1, "scale":"xscale"},
     "x":{"field":"data.x", "scale":"xscale"},
     "y":{"field":"data.y", "scale":"yscale"},
     "y2":{"scale":"yscale", "value":0}},
     "update": { "fill": {"value":"steelblue"} },
     "hover": { "fill": {"value":"red"}}},
   "type":"rect"}],
 "padding":{"bottom":20, "left":30, "right":10, "top":10},
 "scales":
 [{"domain":{"data":"rawData", "field":"data.x"},
   "name":"xscale",
   "range":"width",
   "type":"ordinal"},
  {"domain":{"data":"rawData", "field":"data.y"},
   "name":"yscale",
   "nice":true,
   "range":"height"}],
 "width":400}
