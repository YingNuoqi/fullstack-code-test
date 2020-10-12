const listContainer = document.querySelector('#service-list');
let servicesRequest = new Request('/service');
fetch(servicesRequest)
.then(function(response) { return response.json(); })
.then(function(serviceList) {
  serviceList.forEach(service => {
    const li = document.createElement("li");
    let status = service.status===null ? "UNKNOWN" : service.status;
    li.appendChild(document.createTextNode(service.name + ':' + status + ". (Created at " + service.addTime + ") "));
    // delete button
    const deleteBtn = document.createElement("button");
    deleteBtn.appendChild(document.createTextNode("Delete"));
    li.appendChild(deleteBtn);
    // delete button event
    deleteBtn.addEventListener("click", evt => {
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

const saveButton = document.querySelector('#post-service');
saveButton.onclick = evt => {
    let urlName = document.querySelector('#url-name').value;
    fetch('/service', {
        method: 'post',
        headers: {
        'Accept': 'application/json, text/plain, */*',
        'Content-Type': 'application/json'
        },
        body: JSON.stringify({url:urlName})
    }).then(res=> {
    location.reload()
});
}