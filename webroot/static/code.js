const listContainer = document.querySelector('#service-list');

const statusOkNode = document.getElementsByClassName("status-ok").item(0);
const statusFailNode = document.getElementsByClassName('status-fail').item(0);
const statusUnknownNode = document.getElementsByClassName('status-unknown').item(0);
const deleteBtn = document.getElementsByClassName('delete-btn').item(0);
const addRemarkNode = document.getElementsByClassName('add-remark').item(0);

// get services
let servicesRequest = new Request('/service');

fetch(servicesRequest)
    .then(function (response) {
        return response.json();
    })
    .then(function (serviceList) {
        serviceList.forEach(service => {
            const li = document.createElement("li");
            li.classList.add("collection-item");
            li.classList.add("row");

            // status
            let status = service.status === null ? "UNKNOWN" : service.status;
            let iconNode;
            switch (status) {
                case "OK":
                    iconNode = statusOkNode.cloneNode(true);
                    break;
                case "FAIL":
                    iconNode = statusFailNode.cloneNode(true);
                    break;
                default:
                    iconNode = statusUnknownNode.cloneNode(true);
                    break;
            }
            li.appendChild(iconNode);
            // url + time
            const urlDivNode = document.createElement("div");
            urlDivNode.classList.add("col");
            // url
            const urlTextNode = document.createElement("p");
            urlTextNode.appendChild(document.createTextNode(service.name));
            urlDivNode.appendChild(urlTextNode);
            // time
            const timeNode = document.createElement("p");
            timeNode.classList.add("time-div");
            timeNode.appendChild(document.createTextNode("Created at " + service.addTime));
            urlDivNode.appendChild(timeNode);
            li.appendChild(urlDivNode);

            // name (add remark)
            const remarkDivNode = addRemarkNode.cloneNode(true);
            const remarkInputNode = document.createElement("input");
            remarkInputNode.type = "text";
            remarkInputNode.id = service.name;
            remarkInputNode.classList.add("remark")

            if (service.remark != null) {
                remarkInputNode.value = service.remark;
            }
            remarkInputNode.addEventListener("blur", ev => {
                const remarkVal = document.getElementById(service.name).value;
                if (remarkVal) {
                    console.log(document.getElementById(service.name).value);

                    fetch(`/service`, {
                        method: 'put',
                        headers: {
                            'Accept': 'application/json, text/plain, */*',
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({url: service.name, remark: remarkVal})
                    }).then(res => {
                        // location.reload()
                    });
                }
            });
            remarkDivNode.appendChild(remarkInputNode);
            const remarkLabelNode = document.createElement("label");
            remarkLabelNode.classList.add("active");
            remarkLabelNode.setAttribute("for", service.name);
            remarkLabelNode.appendChild(document.createTextNode("Name Remark"));
            remarkDivNode.appendChild(remarkLabelNode);
            li.appendChild(remarkDivNode);

            // delete button
            const deleteButton = deleteBtn.cloneNode(true);
            li.appendChild(deleteButton);
            // click event
            deleteButton.addEventListener("click", evt => {
                fetch(`/service`, {
                    method: 'delete',
                    body: JSON.stringify({url: service.name})
                }).then(res => {
                    location.reload();
                });
            });

            listContainer.appendChild(li);
        });
    });

// add a service
const saveButton = document.querySelector('#post-service');
saveButton.onclick = evt => {
    let urlName = document.querySelector('#url-name').value;
    fetch('/service', {
        method: 'post',
        headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({url: urlName})
    }).then(res => {
        location.reload()
    });
}