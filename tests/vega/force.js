{
  "name": "force",
  "width": 500,
  "height": 500,
  "padding": {"top":0, "bottom":0, "left":0, "right":0},
  "data": [
    {
      "name": "edges",
      "url": "data/miserables.json",
      "format": {"type": "json", "property": "links"},
      "transform": [
        {"type": "copy", "from": "data", "fields": ["source", "target"]}
      ]
    },
    {
      "name": "nodes",
      "url": "data/miserables.json",
      "format": {"type": "json", "property": "nodes"},
      "transform": [
        {
          "type": "force",
          "links": "edges",
          "linkDistance": 70,
          "charge": -100,
          "iterations": 1000
        }
      ]
    }
  ],
  "marks": [
    {
      "type": "path",
      "from": {
        "data": "edges",
        "transform": [
          {"type": "link", "shape": "line"}
        ]
      },
      "properties": {
        "update": {
          "path": {"field": "path"},
          "stroke": {"value": "#ccc"},
          "strokeWidth": {"value": 0.5}
        }
      }
    },
    {
      "type": "symbol",
      "from": {"data": "nodes"},
      "properties": {
        "enter": {
          "fillOpacity": {"value": 0.3},
          "stroke": {"value": "steelblue"}
        },
        "update": {
          "x": {"field": "x"},
          "y": {"field": "y"},
          "fill": {"value": "steelblue"}
        },
        "hover": {
          "fill": {"value": "#f00"}
        }
      }
    }
  ]
}
